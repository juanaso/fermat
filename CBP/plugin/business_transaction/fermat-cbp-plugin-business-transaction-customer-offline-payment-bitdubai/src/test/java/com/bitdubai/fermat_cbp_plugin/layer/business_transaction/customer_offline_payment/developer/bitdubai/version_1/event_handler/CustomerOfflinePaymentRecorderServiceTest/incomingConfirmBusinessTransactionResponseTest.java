package com.bitdubai.fermat_cbp_plugin.layer.business_transaction.customer_offline_payment.developer.bitdubai.version_1.event_handler.CustomerOfflinePaymentRecorderServiceTest;

import com.bitdubai.fermat_api.layer.all_definition.enums.Plugins;
import com.bitdubai.fermat_api.layer.all_definition.events.interfaces.FermatEventListener;
import com.bitdubai.fermat_cbp_api.all_definition.events.enums.EventType;
import com.bitdubai.fermat_cbp_api.all_definition.exceptions.CantSaveEventException;
import com.bitdubai.fermat_cbp_api.layer.network_service.transaction_transmission.events.IncomingConfirmBusinessTransactionResponse;
import com.bitdubai.fermat_cbp_api.layer.network_service.transaction_transmission.events.IncomingNewContractStatusUpdate;
import com.bitdubai.fermat_cbp_plugin.layer.business_transaction.customer_offline_payment.developer.bitdubai.version_1.database.CustomerOfflinePaymentBusinessTransactionDao;
import com.bitdubai.fermat_cbp_plugin.layer.business_transaction.customer_offline_payment.developer.bitdubai.version_1.event_handler.CustomerOfflinePaymentRecorderService;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.interfaces.ErrorManager;
import com.bitdubai.fermat_pip_api.layer.platform_service.event_manager.interfaces.EventManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

/**
 * Created by alexander jimenez (alex_jimenez76@hotmail.com) on 16/02/16.
 */
public class incomingConfirmBusinessTransactionResponseTest {
    @Mock
    CustomerOfflinePaymentBusinessTransactionDao customerOfflinePaymentBusinessTransactionDao;
    @Mock
    EventManager eventManager;
    @Mock
    ErrorManager errorManager;
    @Mock
    FermatEventListener mockFermatEventListener;
    IncomingConfirmBusinessTransactionResponse incomingConfirmBusinessTransactionResponse;
    CustomerOfflinePaymentRecorderService customerOfflinePaymentRecorderService;

    public void setUpGeneralMockitoRules() throws Exception{
        when(eventManager.getNewListener(EventType.INCOMING_NEW_CONTRACT_STATUS_UPDATE)).thenReturn(mockFermatEventListener);
        when(eventManager.getNewListener(EventType.INCOMING_CONFIRM_BUSINESS_TRANSACTION_RESPONSE)).thenReturn(mockFermatEventListener);


    }
    @Before
    public void setup()throws Exception{
        MockitoAnnotations.initMocks(this);
        setUpGeneralMockitoRules();
    }
    @Test
    public void incomingConfirmBusinessTransactionResponseTest_Should_Return_() throws Exception {
        //when(incomingConfirmBusinessTransactionResponse.getRemoteBusinessTransaction()).thenReturn(Plugins.CUSTOMER_OFFLINE_PAYMENT);
        customerOfflinePaymentRecorderService = new CustomerOfflinePaymentRecorderService(customerOfflinePaymentBusinessTransactionDao,eventManager,errorManager);
        //customerOfflinePaymentRecorderService.incomingConfirmBusinessTransactionResponse(incomingConfirmBusinessTransactionResponse);
    }

    @Test(expected = Exception.class)
    public void incomingConfirmBusinessTransactionResponseTest_Should_Throw_Exception() throws Exception {
        customerOfflinePaymentRecorderService = new CustomerOfflinePaymentRecorderService(customerOfflinePaymentBusinessTransactionDao,eventManager,errorManager);
        customerOfflinePaymentRecorderService.setEventManager(eventManager);
        customerOfflinePaymentRecorderService.start();
        customerOfflinePaymentRecorderService.incomingConfirmBusinessTransactionResponse(null);
    }

    @Test(expected = CantSaveEventException.class)
    public void incomingConfirmBusinessTransactionResponseTest_Should_Throw_CantSaveEventException() throws Exception {
        customerOfflinePaymentRecorderService = new CustomerOfflinePaymentRecorderService(customerOfflinePaymentBusinessTransactionDao,eventManager,errorManager);
        customerOfflinePaymentRecorderService.setEventManager(eventManager);
        customerOfflinePaymentRecorderService.start();
        customerOfflinePaymentRecorderService.incomingConfirmBusinessTransactionResponse(incomingConfirmBusinessTransactionResponse);
    }
}