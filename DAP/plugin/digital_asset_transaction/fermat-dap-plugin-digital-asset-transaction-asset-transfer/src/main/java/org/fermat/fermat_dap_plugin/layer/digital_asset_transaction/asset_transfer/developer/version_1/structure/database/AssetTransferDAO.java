package org.fermat.fermat_dap_plugin.layer.digital_asset_transaction.asset_transfer.developer.version_1.structure.database;

import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.all_definition.enums.BlockchainNetworkType;
import com.bitdubai.fermat_api.layer.all_definition.events.EventSource;
import com.bitdubai.fermat_api.layer.all_definition.exceptions.InvalidParameterException;
import com.bitdubai.fermat_api.layer.all_definition.transaction_transference_protocol.ProtocolStatus;
import com.bitdubai.fermat_api.layer.all_definition.transaction_transference_protocol.crypto_transactions.CryptoStatus;
import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseFilterOrder;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseFilterType;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTable;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTableRecord;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantExecuteQueryException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantInsertRecordException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantLoadTableToMemoryException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantOpenDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantUpdateRecordException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseNotFoundException;
import org.fermat.fermat_dap_api.layer.all_definition.enums.DistributionStatus;
import org.fermat.fermat_dap_api.layer.all_definition.enums.EventStatus;
import org.fermat.fermat_dap_api.layer.all_definition.enums.TransactionStatus;
import org.fermat.fermat_dap_api.layer.dap_actor.asset_user.interfaces.ActorAssetUser;
import org.fermat.fermat_dap_api.layer.dap_transaction.common.exceptions.CantExecuteDatabaseOperationException;
import org.fermat.fermat_dap_api.layer.dap_transaction.common.exceptions.CantPersistDigitalAssetException;
import org.fermat.fermat_dap_api.layer.dap_transaction.common.exceptions.CantPersistsTransactionUUIDException;
import org.fermat.fermat_dap_api.layer.dap_transaction.common.exceptions.CantSaveEventException;
import org.fermat.fermat_dap_api.layer.dap_transaction.common.exceptions.CantStartDeliveringException;
import org.fermat.fermat_dap_api.layer.dap_transaction.common.exceptions.RecordsNotFoundException;
import org.fermat.fermat_dap_api.layer.dap_transaction.common.exceptions.UnexpectedResultReturnedFromDatabaseException;
import org.fermat.fermat_dap_plugin.layer.digital_asset_transaction.asset_transfer.developer.version_1.exceptions.CantCheckTransferProgressException;
import org.fermat.fermat_dap_plugin.layer.digital_asset_transaction.asset_transfer.developer.version_1.structure.functional.DeliverRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Víctor Mars (marsvicam@gmail.com) on 08/01/16.
 */
public class AssetTransferDAO {

    private final org.fermat.fermat_dap_plugin.layer.digital_asset_transaction.asset_transfer.developer.version_1.structure.functional.DigitalAssetTransferVault digitalAssetTransferVault;
    //VARIABLE DECLARATION
    private UUID pluginId;
    private Database database;
    private PluginDatabaseSystem pluginDatabaseSystem;


    //CONSTRUCTORS
    public AssetTransferDAO(PluginDatabaseSystem pluginDatabaseSystem,
                            UUID pluginId,
                            org.fermat.fermat_dap_plugin.layer.digital_asset_transaction.asset_transfer.developer.version_1.structure.functional.DigitalAssetTransferVault digitalAssetTransferVault) throws CantExecuteDatabaseOperationException {
        this.pluginDatabaseSystem = pluginDatabaseSystem;
        this.pluginId = pluginId;
        this.digitalAssetTransferVault = digitalAssetTransferVault;
        database = openDatabase();
    }

    //PUBLIC METHODS
    public void startDelivering(String genesisTransaction,
                                String assetPublicKey,
                                String userPublicKey,
                                BlockchainNetworkType networkType) throws CantStartDeliveringException {
        String context = "Genesis Transaction: " + genesisTransaction + " - Asset Public Key: " + assetPublicKey + " - User Public Key: " + userPublicKey;

        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        long timeOut = startTime + org.fermat.fermat_dap_plugin.layer.digital_asset_transaction.asset_transfer.developer.version_1.AssetTransferDigitalAssetTransactionPluginRoot.DELIVERING_TIMEOUT;

        try {
            DatabaseTable databaseTable = getDatabaseTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME);
            DatabaseTableRecord record = databaseTable.getEmptyRecord();
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME, transactionId);
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_GENESIS_TRANSACTION_COLUMN_NAME, genesisTransaction);
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_NETWORK_TYPE_COLUMN_NAME, networkType.getCode());
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_ASSET_PUBLICKEY_COLUMN_NAME, assetPublicKey);
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_USER_PUBLICKEY_COLUMN_NAME, userPublicKey);
            record.setLongValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_START_TIME_COLUMN_NAME, startTime);
            record.setLongValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TIMEOUT_COLUMN_NAME, timeOut);
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME, DistributionStatus.DELIVERING.getCode());
            record.setLongValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_ATTEMPT_NUMBER_COLUMN_NAME, 0);

            databaseTable.insertRecord(record);
        } catch (CantInsertRecordException exception) {
            throw new CantStartDeliveringException(exception, context, "Starting the delivering at transfer");
        }
    }

    public void persistDigitalAsset(String transactionHash,
                                    String localStoragePath,
                                    String digitalAssetHash,
                                    String actorReceiverPublicKey,
                                    String actorReceiverBitcoinAddress) throws CantPersistDigitalAssetException {
        try {

            DatabaseTable databaseTable = getDatabaseTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME);
            DatabaseTableRecord record = databaseTable.getEmptyRecord();
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME, transactionHash);
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DIGITAL_ASSET_HASH_COLUMN_NAME, digitalAssetHash);
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DIGITAL_ASSET_STORAGE_LOCAL_PATH_COLUMN_NAME, localStoragePath);
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_ACTOR_ASSET_USER_PUBLIC_KEY_COLUMN_NAME, actorReceiverPublicKey);
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_TRANSFERENCE_STATUS_COLUMN_NAME, DistributionStatus.CHECKING_HASH.getCode());
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_PROTOCOL_STATUS_COLUMN_NAME, ProtocolStatus.TO_BE_NOTIFIED.getCode());
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_CRYPTO_STATUS_COLUMN_NAME, CryptoStatus.PENDING_SUBMIT.getCode());
            record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_ACTOR_ASSET_USER_BITCOIN_ADDRESS_COLUMN_NAME, actorReceiverBitcoinAddress);
            databaseTable.insertRecord(record);

        } catch (CantInsertRecordException exception) {
            throw new CantPersistDigitalAssetException(exception, "Persisting a forming genesis digital asset", "Cannot insert a record in the Asset Distribution database");
        } catch (Exception exception) {
            throw new CantPersistDigitalAssetException(exception, "Persisting a forming genesis digital asset", "Unexpected exception");
        }
    }

    public void updateActorAssetUser(ActorAssetUser user, String genesisTransaction) throws CantUpdateRecordException, CantLoadTableToMemoryException, RecordsNotFoundException {
        String context = "User: " + user + " - Genesis Tx: " + genesisTransaction;
        DatabaseTable databaseTable = this.database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME);
        databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME, genesisTransaction, DatabaseFilterType.EQUAL);
        databaseTable.loadToMemory();
        List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
        if (databaseTableRecords.isEmpty()) {
            throw new RecordsNotFoundException(null, context, null);
        }

        DatabaseTableRecord record = databaseTableRecords.get(0);
        record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_ACTOR_ASSET_USER_PUBLIC_KEY_COLUMN_NAME, user.getActorPublicKey());
        record.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_ACTOR_ASSET_USER_BITCOIN_ADDRESS_COLUMN_NAME, user.getCryptoAddress().getAddress());
        databaseTable.updateRecord(record);
    }

    public void updateDistributionStatusByGenesisTransaction(DistributionStatus transferStatus, String genesisTransaction) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        updateStringValueByStringFieldDistributionTable(transferStatus.getCode(),
                AssetTransferDatabaseConstants.ASSET_TRANSFER_TRANSFERENCE_STATUS_COLUMN_NAME,
                genesisTransaction,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME);
    }


    public void saveNewEvent(String eventType, String eventSource) throws CantSaveEventException {
        try {

            DatabaseTable databaseTable = this.database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TABLE_NAME);
            DatabaseTableRecord eventRecord = databaseTable.getEmptyRecord();
            UUID eventRecordID = UUID.randomUUID();
            long unixTime = System.currentTimeMillis();
            eventRecord.setUUIDValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_ID_COLUMN_NAME, eventRecordID);
            eventRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_EVENT_COLUMN_NAME, eventType);
            eventRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_SOURCE_COLUMN_NAME, eventSource);
            eventRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_STATUS_COLUMN_NAME, EventStatus.PENDING.getCode());
            eventRecord.setLongValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TIMESTAMP_COLUMN_NAME, unixTime);
            databaseTable.insertRecord(eventRecord);

        } catch (CantInsertRecordException exception) {

            throw new CantSaveEventException(exception, "Saving new event.", "Cannot insert a record in Asset Distribution database");
        } catch (Exception exception) {

            throw new CantSaveEventException(FermatException.wrapException(exception), "Saving new event.", "Unexpected exception");
        }
    }

    public void sendingBitcoins(String genesisTransaction, String bitcoinsSentGenesisTx) throws RecordsNotFoundException, CantCheckTransferProgressException {
        try {
            DatabaseTable databaseTable;
            databaseTable = database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_GENESIS_TRANSACTION_COLUMN_NAME, genesisTransaction, DatabaseFilterType.EQUAL);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME, DistributionStatus.DELIVERING.getCode(), DatabaseFilterType.EQUAL);
            databaseTable.addFilterOrder(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_START_TIME_COLUMN_NAME, DatabaseFilterOrder.DESCENDING);
            databaseTable.loadToMemory();
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            DatabaseTableRecord databaseTableRecord;
            if (databaseTable.getRecords().isEmpty()) {
                throw new RecordsNotFoundException(null, "Genesis Tx: " + genesisTransaction, "There is nothing to update.");
            }

            databaseTableRecord = databaseTableRecords.get(0);

            databaseTableRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_SENT_GENESISTX_COLUMN_NAME, bitcoinsSentGenesisTx);
            databaseTableRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME, DistributionStatus.SENDING_CRYPTO.getCode());
            databaseTable.updateRecord(databaseTableRecord);
        } catch (CantLoadTableToMemoryException | CantUpdateRecordException exception) {
            throw new CantCheckTransferProgressException(exception, "Updating Crypto Status ", "Cannot load the table into memory");
        }
    }

    public void updateDeliveringStatusForTxId(String transactionId, DistributionStatus status) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException, RecordsNotFoundException {
        try {
            DatabaseTable databaseTable;
            databaseTable = database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME, transactionId, DatabaseFilterType.EQUAL);
            databaseTable.addFilterOrder(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_START_TIME_COLUMN_NAME, DatabaseFilterOrder.DESCENDING);
            databaseTable.loadToMemory();
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            DatabaseTableRecord databaseTableRecord;
            if (databaseTable.getRecords().isEmpty()) {
                throw new RecordsNotFoundException(null, "Tx Id: " + transactionId, "There is nothing to update.");
            }

            databaseTableRecord = databaseTableRecords.get(0);

            databaseTableRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME, status.getCode());
            databaseTable.updateRecord(databaseTableRecord);
        } catch (CantLoadTableToMemoryException | CantUpdateRecordException exception) {
            throw new CantCheckTransferProgressException(exception, "Updating Crypto Status ", "Cannot load the table into memory");
        }
    }


    public void updateDigitalAssetCryptoStatusByGenesisTransaction(String genesisTransaction, CryptoStatus cryptoStatus) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {
        try {

            DatabaseTable databaseTable;
            databaseTable = database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME, genesisTransaction, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            DatabaseTableRecord databaseTableRecord;
            if (databaseTableRecords.size() > 1) {

                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", "Genesis Transaction:" + genesisTransaction);
            } else {
                databaseTableRecord = databaseTableRecords.get(0);
            }
            databaseTableRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_CRYPTO_STATUS_COLUMN_NAME, cryptoStatus.getCode());
            databaseTable.updateRecord(databaseTableRecord);

        } catch (CantLoadTableToMemoryException exception) {

            throw new CantCheckTransferProgressException(exception, "Updating Crypto Status ", "Cannot load the table into memory");
        } catch (Exception exception) {

            throw new CantCheckTransferProgressException(FermatException.wrapException(exception), "Updating Crypto Status.", "Unexpected exception - Transaction hash:" + genesisTransaction);
        }
    }

    public DistributionStatus getDistributionStatusForGenesisTx(String genesisTx) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException, InvalidParameterException {
        return DistributionStatus.getByCode(getStringValueFromSelectedTableTableByFieldCode(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME, genesisTx, AssetTransferDatabaseConstants.ASSET_TRANSFER_TRANSFERENCE_STATUS_COLUMN_NAME, AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME));
    }

    public void updateEventStatus(String eventId) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        try {

            DatabaseTable databaseTable = this.database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_ID_COLUMN_NAME, eventId, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            DatabaseTableRecord databaseTableRecord;
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            if (databaseTableRecords.size() > 1) {
                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", "Event ID:" + eventId);
            } else {
                databaseTableRecord = databaseTableRecords.get(0);
            }
            databaseTableRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_STATUS_COLUMN_NAME, EventStatus.NOTIFIED.getCode());
            databaseTable.updateRecord(databaseTableRecord);

        } catch (CantLoadTableToMemoryException exception) {
            throw new CantExecuteQueryException(CantLoadTableToMemoryException.DEFAULT_MESSAGE, exception, "Trying to update " + AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TABLE_NAME, "Check the cause");
        } catch (Exception exception) {
            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), "Trying to update " + AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TABLE_NAME, "Check the cause");
        }
    }

    public void persistDistributionId(String genesisTransaction, String transferId) throws CantPersistsTransactionUUIDException {
        try {

            DatabaseTable databaseTable = getDatabaseTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME, genesisTransaction, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            DatabaseTableRecord databaseTableRecord;
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            if (databaseTableRecords.size() > 1) {

                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", "GenesisTransaction:" + genesisTransaction + " OutgoingId:" + transferId);
            } else {
                databaseTableRecord = databaseTableRecords.get(0);
            }
            databaseTableRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_TRANSFERENCE_ID_COLUMN_NAME, transferId);
            databaseTableRecord.setStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_TRANSFERENCE_STATUS_COLUMN_NAME, TransactionStatus.TO_DELIVER.getCode());
            databaseTable.updateRecord(databaseTableRecord);

        } catch (Exception exception) {

            throw new CantPersistsTransactionUUIDException(FermatException.wrapException(exception), "Persisting transferId in database", "Unexpected exception");
        }
    }

    public void cancelDelivering(String transactionId) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        updateDeliveringStatus(DistributionStatus.DELIVERING_CANCELLED, transactionId);
    }

    public void failedToSendCrypto(String transactionId) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        updateDeliveringStatus(DistributionStatus.SENDING_CRYPTO_FAILED, transactionId);
    }

    public void newAttempt(String transactionId) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException, CantCheckTransferProgressException {
        long attemptNumber = constructRecordFromTransactionId(transactionId).getAttemptNumber();
        updateLongValueByStringFieldDeliveringTable(++attemptNumber, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_ATTEMPT_NUMBER_COLUMN_NAME, transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME);
    }

    public boolean isFirstTransaction(String genesisTransaction) throws CantCheckTransferProgressException {
        return getValueListFromTableByColumn(genesisTransaction,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME
        ).isEmpty();
    }

    public boolean isDeliveringGenesisTransaction(String genesisTransaction) throws CantCheckTransferProgressException {
        return !getDeliveringTransactionsFromGenesisTransaction(genesisTransaction).isEmpty();
    }

    public DeliverRecord getLastDelivering(String genesisTx) throws CantCheckTransferProgressException {
        List<DeliverRecord> records = getDeliverRecordsForGenesisTransaction(genesisTx);
        return records.get(records.size() - 1);
    }

    public List<DeliverRecord> getSendingCryptoRecords() throws CantCheckTransferProgressException {
        List<DeliverRecord> toReturn = new ArrayList<>();
        for (String txId : getValueListFromTableByColumn(DistributionStatus.SENDING_CRYPTO.getCode(),
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME)) {
            toReturn.add(constructRecordFromTransactionId(txId));
        }
        return toReturn;
    }

    public List<DeliverRecord> getDeliveredRecords() throws CantCheckTransferProgressException {
        List<DeliverRecord> toReturn = new ArrayList<>();
        for (String txId : getValueListFromTableByColumn(DistributionStatus.DELIVERED.getCode(),
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME)) {
            toReturn.add(constructRecordFromTransactionId(txId));
        }
        return toReturn;
    }

    public List<DeliverRecord> getDeliveringRecords() throws CantCheckTransferProgressException {
        List<DeliverRecord> toReturn = new ArrayList<>();
        for (String txId : getValueListFromTableByColumn(DistributionStatus.DELIVERING.getCode(),
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME)) {
            toReturn.add(constructRecordFromTransactionId(txId));
        }
        return toReturn;
    }

    public List<DeliverRecord> getDeliverRecordsForGenesisTransaction(String genesisTransaction) throws CantCheckTransferProgressException {
        List<DeliverRecord> toReturn = new ArrayList<>();
        for (String txId : getValueListFromTableByColumn(genesisTransaction,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_GENESIS_TRANSACTION_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME)) {
            toReturn.add(constructRecordFromTransactionId(txId));
        }
        return toReturn;
    }

    //PRIVATE METHODS
    private String getStringFieldByDeliveringId(String deliveringId, String column) throws RecordsNotFoundException, CantLoadTableToMemoryException {
        String context = "Tx Id: " + deliveringId;

        DatabaseTable databaseTable;
        databaseTable = database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME);
        databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME, deliveringId, DatabaseFilterType.EQUAL);
        databaseTable.loadToMemory();

        if (databaseTable.getRecords().isEmpty()) throw new RecordsNotFoundException(context);

        return databaseTable.getRecords().get(0).getStringValue(column);
    }

    private long getLongFieldByDeliveringId(String deliveringId, String column) throws RecordsNotFoundException, CantLoadTableToMemoryException {
        String context = "Tx Id: " + deliveringId;

        DatabaseTable databaseTable;
        databaseTable = database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME);
        databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME, deliveringId, DatabaseFilterType.EQUAL);
        databaseTable.loadToMemory();

        if (databaseTable.getRecords().isEmpty()) throw new RecordsNotFoundException(context);

        return databaseTable.getRecords().get(0).getLongValue(column);
    }

    private List<String> getDeliveringIdByStatus(DistributionStatus status, String genesisTransaction) throws CantCheckTransferProgressException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_GENESIS_TRANSACTION_COLUMN_NAME, genesisTransaction);
        filters.put(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME, status.getCode());
        return getValueListFromTableByColumn(filters, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME);
    }

    private void updateDeliveringStatus(DistributionStatus status, String transactionId) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        updateStringValueByStringFieldDeliveringTable(status.getCode(), AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME, transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TRANSACTION_ID_COLUMN_NAME);
    }

    //TODO HEREE
    private List<String> getDeliveringTransactionsFromGenesisTransaction(String genesisTransaction) throws CantCheckTransferProgressException {
        return getDeliveringIdByStatus(DistributionStatus.DELIVERING, genesisTransaction);
    }

    private DatabaseTable getDatabaseTable(String tableName) {
        return database.getTable(tableName);
    }

    private Database openDatabase() throws CantExecuteDatabaseOperationException {
        try {
            return pluginDatabaseSystem.openDatabase(pluginId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DATABASE);
        } catch (CantOpenDatabaseException | DatabaseNotFoundException exception) {
            throw new CantExecuteDatabaseOperationException(exception, "Opening the Asset Distribution Transaction Database", "Error in database plugin.");
        }
    }

    private void updateStringValueByStringFieldDeliveringTable(String value, String columnName, String filterValue, String filterColumn) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        try {
            DatabaseTable databaseTable = this.database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME);
            databaseTable.addStringFilter(filterColumn, filterValue, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            DatabaseTableRecord databaseTableRecord;
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            if (databaseTableRecords.size() > 1) {
                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", filterColumn + ": " + filterColumn);
            } else {
                databaseTableRecord = databaseTableRecords.get(0);
            }
            databaseTableRecord.setStringValue(columnName, value);
            databaseTable.updateRecord(databaseTableRecord);
        } catch (CantLoadTableToMemoryException exception) {
            throw new CantExecuteQueryException(CantLoadTableToMemoryException.DEFAULT_MESSAGE, exception, "Trying to update " + columnName, "Check the cause");
        } catch (Exception exception) {
            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), "Trying to update " + columnName, "Check the cause");
        }
    }

    private void updateLongValueByStringFieldDeliveringTable(long value, String columnName, String filterValue, String filterColumn) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        try {
            DatabaseTable databaseTable = this.database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TABLE_NAME);
            databaseTable.addStringFilter(filterColumn, filterValue, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            DatabaseTableRecord databaseTableRecord;
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            if (databaseTableRecords.size() > 1) {
                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", filterColumn + ": " + filterColumn);
            } else {
                databaseTableRecord = databaseTableRecords.get(0);
            }
            databaseTableRecord.setLongValue(columnName, value);
            databaseTable.updateRecord(databaseTableRecord);
        } catch (CantLoadTableToMemoryException exception) {
            throw new CantExecuteQueryException(CantLoadTableToMemoryException.DEFAULT_MESSAGE, exception, "Trying to update " + columnName, "Check the cause");
        } catch (Exception exception) {
            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), "Trying to update " + columnName, "Check the cause");
        }
    }

    private void updateStringValueByStringFieldDistributionTable(String value, String columnName, String filterValue, String filterColumn) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        try {

            DatabaseTable databaseTable = this.database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME);
            databaseTable.addStringFilter(filterColumn, filterValue, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            DatabaseTableRecord databaseTableRecord;
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            if (databaseTableRecords.size() > 1) {
                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", filterColumn + ": " + filterColumn);
            } else {
                databaseTableRecord = databaseTableRecords.get(0);
            }
            databaseTableRecord.setStringValue(columnName, value);
            databaseTable.updateRecord(databaseTableRecord);
        } catch (CantLoadTableToMemoryException exception) {

            throw new CantExecuteQueryException(CantLoadTableToMemoryException.DEFAULT_MESSAGE, exception, "Trying to update " + columnName, "Check the cause");
        } catch (Exception exception) {

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), "Trying to update " + columnName, "Check the cause");
        }
    }

    private boolean isPendingEventsBySource(EventSource eventSource) throws CantExecuteQueryException {
        try {

            DatabaseTable databaseTable;
            databaseTable = database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_STATUS_COLUMN_NAME, EventStatus.PENDING.getCode(), DatabaseFilterType.EQUAL);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_SOURCE_COLUMN_NAME, eventSource.getCode(), DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();

            return !databaseTable.getRecords().isEmpty();
        } catch (CantLoadTableToMemoryException exception) {

            throw new CantExecuteQueryException("Error executing query in DB.", exception, "Getting pending events.", "Cannot load table to memory.");
        } catch (Exception exception) {

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), "Getting pending events.", "Unexpected exception");
        }
    }

    private List<String> getPendingEventsBySource(EventSource eventSource) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {

        try {

            List<String> eventIdList = new ArrayList<>();
            DatabaseTable databaseTable = getDatabaseTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_STATUS_COLUMN_NAME, EventStatus.PENDING.getCode(), DatabaseFilterType.EQUAL);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_SOURCE_COLUMN_NAME, eventSource.getCode(), DatabaseFilterType.EQUAL);
            databaseTable.addFilterOrder(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TIMESTAMP_COLUMN_NAME, DatabaseFilterOrder.ASCENDING);
            databaseTable.loadToMemory();
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            for (DatabaseTableRecord databaseTableRecord : databaseTableRecords) {
                String eventId = databaseTableRecord.getStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_ID_COLUMN_NAME);
                eventIdList.add(eventId);
            }

            return eventIdList;
        } catch (CantLoadTableToMemoryException exception) {

            throw new CantCheckTransferProgressException(exception, "Trying to get pending events", "Cannot load the database into memory");
        } catch (Exception exception) {

            throw new CantCheckTransferProgressException(FermatException.wrapException(exception), "Trying to get pending events.", "Unexpected exception");
        }
    }

    private List<String> getGenesisTransactionByDistributionStatus(DistributionStatus transferStatus) throws CantCheckTransferProgressException {
        return getValueListFromTableByColumn(transferStatus.getCode(),
                AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_TRANSFERENCE_STATUS_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME);
    }

    private List<String> getValueListFromTableByColumn(String filterValue, String table, String filterColumn, String returningColumn) throws CantCheckTransferProgressException {
        try {

            DatabaseTable databaseTable;
            List<String> returningList = new ArrayList<>();
            databaseTable = database.getTable(table);
            databaseTable.addStringFilter(filterColumn, filterValue, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            for (DatabaseTableRecord record : databaseTable.getRecords()) {
                returningList.add(record.getStringValue(returningColumn));
            }
            return returningList;
        } catch (CantLoadTableToMemoryException exception) {

            throw new CantCheckTransferProgressException(exception, "Getting " + filterColumn + " list", "Cannot load table to memory");
        } catch (Exception exception) {

            throw new CantCheckTransferProgressException(FermatException.wrapException(exception), "Getting " + filterColumn + " list", "Unexpected exception");
        }
    }

    private List<String> getValueListFromTableByColumn(Map<String, String> filters, String table, String returningColumn) throws CantCheckTransferProgressException {
        try {
            DatabaseTable databaseTable;
            List<String> returningList = new ArrayList<>();
            databaseTable = database.getTable(table);
            for (Map.Entry<String, String> filter : filters.entrySet()) {
                String filterColumn = filter.getKey();
                String filterValue = filter.getValue();
                databaseTable.addStringFilter(filterColumn, filterValue, DatabaseFilterType.EQUAL);
            }

            databaseTable.addFilterOrder(AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_START_TIME_COLUMN_NAME, DatabaseFilterOrder.DESCENDING);

            databaseTable.loadToMemory();
            for (DatabaseTableRecord record : databaseTable.getRecords()) {
                returningList.add(record.getStringValue(returningColumn));
            }
            return returningList;
        } catch (CantLoadTableToMemoryException exception) {
            throw new CantCheckTransferProgressException(exception, "Getting " + filters + " list", "Cannot load table to memory");
        } catch (Exception exception) {
            throw new CantCheckTransferProgressException(FermatException.wrapException(exception), "Getting " + filters + " list", "Unexpected exception");
        }
    }

    /**
     * This method returns a String value from the fieldCode, filtered by value in indexColumn
     *
     * @param tableName   table name
     * @param value       value used as filter
     * @param fieldCode   column that contains the required value
     * @param indexColumn the column filter
     * @return a String with the required value
     * @throws CantCheckTransferProgressException
     * @throws UnexpectedResultReturnedFromDatabaseException
     */
    private String getStringValueFromSelectedTableTableByFieldCode(String tableName, String value, String fieldCode, String indexColumn) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {
        try {
            DatabaseTable databaseTable = getDatabaseTable(tableName);
            databaseTable.addStringFilter(indexColumn, value, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            DatabaseTableRecord databaseTableRecord;
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            if (databaseTableRecords.size() > 1) {

                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", indexColumn + ":" + value);
            } else {
                databaseTableRecord = databaseTableRecords.get(0);
            }

            return databaseTableRecord.getStringValue(fieldCode);
        } catch (CantLoadTableToMemoryException exception) {

            throw new CantCheckTransferProgressException(exception, "Trying to get " + fieldCode, "Cannot load the database into memory");
        }
    }

    private DeliverRecord constructRecordFromTransactionId(String transactionId) throws CantCheckTransferProgressException {
        try {
            DeliverRecord recordToReturn = new DeliverRecord();
            recordToReturn.setTransactionId(transactionId);
            recordToReturn.setGenesisTransaction(getStringFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_GENESIS_TRANSACTION_COLUMN_NAME));
            recordToReturn.setNetworkType(BlockchainNetworkType.getByCode(getStringFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_NETWORK_TYPE_COLUMN_NAME)));
            recordToReturn.setActorAssetUserPublicKey(getStringFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_USER_PUBLICKEY_COLUMN_NAME));
            recordToReturn.setDigitalAssetMetadata(digitalAssetTransferVault.getDigitalAssetMetadataFromWallet(getStringFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_GENESIS_TRANSACTION_COLUMN_NAME), BlockchainNetworkType.getByCode(getStringFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_NETWORK_TYPE_COLUMN_NAME))));
            recordToReturn.setStartTime(new Date(getLongFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_START_TIME_COLUMN_NAME)));
            recordToReturn.setTimeOut(new Date(getLongFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_TIMEOUT_COLUMN_NAME)));
            recordToReturn.setState(DistributionStatus.getByCode(getStringFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_STATE_COLUMN_NAME)));
            recordToReturn.setGenesisTransactionSent(getStringFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_SENT_GENESISTX_COLUMN_NAME));
            recordToReturn.setAttemptNumber(getLongFieldByDeliveringId(transactionId, AssetTransferDatabaseConstants.ASSET_TRANSFER_DELIVERING_ATTEMPT_NUMBER_COLUMN_NAME));
            return recordToReturn;
        } catch (Exception e) {
            throw new CantCheckTransferProgressException(e, transactionId, null);
        }
    }

    //GETTERS AND SETTERS

    public boolean isPendingNetworkLayerEvents() throws CantExecuteQueryException {
        return isPendingEventsBySource(EventSource.NETWORK_SERVICE_ASSET_TRANSMISSION);
    }

    public boolean isPendingIncomingCryptoEvents() throws CantExecuteQueryException {
        return isPendingEventsBySource(EventSource.CRYPTO_ROUTER);
    }


    public List<String> getPendingNetworkLayerEvents() throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {
        return getPendingEventsBySource(EventSource.NETWORK_SERVICE_ASSET_TRANSMISSION);
    }

    public List<String> getPendingCryptoRouterEvents() throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {
        return getPendingEventsBySource(EventSource.CRYPTO_ROUTER);
    }

    public String getEventTypeById(String eventId) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {

        try {

            DatabaseTable databaseTable = getDatabaseTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_ID_COLUMN_NAME, eventId, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();
            List<DatabaseTableRecord> databaseTableRecords = databaseTable.getRecords();
            DatabaseTableRecord databaseTableRecord;
            if (databaseTableRecords.size() > 1) {

                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", "Event Id" + eventId);
            } else {
                databaseTableRecord = databaseTableRecords.get(0);
            }

            return databaseTableRecord.getStringValue(AssetTransferDatabaseConstants.ASSET_TRANSFER_EVENTS_RECORDED_EVENT_COLUMN_NAME);
        } catch (CantLoadTableToMemoryException exception) {

            throw new CantCheckTransferProgressException(exception, "Trying to get pending events", "Cannot load the database into memory");
        } catch (Exception exception) {

            throw new CantCheckTransferProgressException(FermatException.wrapException(exception), "Trying to get pending events.", "Unexpected exception");
        }
    }

    public List<String> getGenesisTransactionByAssetAcceptedStatus() throws CantCheckTransferProgressException {
        return getGenesisTransactionByDistributionStatus(DistributionStatus.ASSET_ACCEPTED);
    }

    public List<String> getGenesisTransactionByDeliveredStatus() throws CantCheckTransferProgressException {
        return getGenesisTransactionByDistributionStatus(DistributionStatus.DELIVERED);
    }

    public List<String> getGenesisTransactionByAssetRejectedByContractStatus() throws CantCheckTransferProgressException {
        return getGenesisTransactionByDistributionStatus(DistributionStatus.ASSET_REJECTED_BY_CONTRACT);
    }

    public List<String> getGenesisTransactionByAssetRejectedByHashStatus() throws CantCheckTransferProgressException {
        return getGenesisTransactionByDistributionStatus(DistributionStatus.ASSET_REJECTED_BY_HASH);
    }

    public List<String> getGenesisTransactionListByCryptoStatus(CryptoStatus cryptoStatus) throws CantCheckTransferProgressException {
        return getValueListFromTableByColumn(cryptoStatus.getCode(),
                AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_CRYPTO_STATUS_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME);
    }

    public String getActorUserPublicKeyByGenesisTransaction(String genesisTransaction) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {
        return getStringValueFromSelectedTableTableByFieldCode(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME,
                genesisTransaction,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_ACTOR_ASSET_USER_PUBLIC_KEY_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME);
    }

    public String getPublicKeyByGenesisTransaction(String genesisTransaction) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {
        return getStringValueFromSelectedTableTableByFieldCode(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME,
                genesisTransaction,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_TRANSFERENCE_ID_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME);
    }

    public String getTransactionIdByGenesisTransaction(String genesisTransaction) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {
        return getStringValueFromSelectedTableTableByFieldCode(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME,
                genesisTransaction,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_TRANSFERENCE_ID_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME);
    }

    public String getActorUserCryptoAddressByGenesisTransaction(String genesisTransaction) throws CantCheckTransferProgressException, UnexpectedResultReturnedFromDatabaseException {
        return getStringValueFromSelectedTableTableByFieldCode(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME,
                genesisTransaction,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_ACTOR_ASSET_USER_BITCOIN_ADDRESS_COLUMN_NAME,
                AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME);
    }


    public boolean isPendingTransactions(CryptoStatus cryptoStatus) throws CantExecuteQueryException {
        try {

            DatabaseTable databaseTable;
            databaseTable = database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_PROTOCOL_STATUS_COLUMN_NAME, ProtocolStatus.TO_BE_NOTIFIED.getCode(), DatabaseFilterType.EQUAL);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_CRYPTO_STATUS_COLUMN_NAME, cryptoStatus.getCode(), DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();

            return !databaseTable.getRecords().isEmpty();
        } catch (CantLoadTableToMemoryException exception) {

            throw new CantExecuteQueryException("Error executing query in DB.", exception, "Getting pending transactions.", "Cannot load table to memory.");
        } catch (Exception exception) {

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), "Getting pending transactions.", "Unexpected exception");
        }
    }

    public boolean isGenesisTransactionRegistered(String genesisTransaction) throws CantExecuteQueryException {
        try {

            DatabaseTable databaseTable;
            databaseTable = database.getTable(AssetTransferDatabaseConstants.ASSET_TRANSFER_TABLE_NAME);
            databaseTable.addStringFilter(AssetTransferDatabaseConstants.ASSET_TRANSFER_GENESIS_TRANSACTION_COLUMN_NAME, genesisTransaction, DatabaseFilterType.EQUAL);
            databaseTable.loadToMemory();

            return !databaseTable.getRecords().isEmpty();
        } catch (CantLoadTableToMemoryException exception) {

            throw new CantExecuteQueryException("Error executing query in DB.", exception, "Checking if genesis transaction exists in database.", "Cannot load table to memory.");
        } catch (Exception exception) {

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), "Checking if genesis transaction exits in database.", "Unexpected exception");
        }
    }

}
