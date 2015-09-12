package unit.com.bitdubait.fermat_dmp_plugin.layer.network_service.wallet_resources.developer.bitdubai.version_1.WalletResourcesNetworkServicePluginRoot;

import com.bitdubai.fermat_api.CantStartPluginException;
import com.bitdubai.fermat_api.layer.all_definition.enums.ServiceStatus;

import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantOpenDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseNotFoundException;
import com.bitdubai.fermat_api.layer.osa_android.logger_system.LogManager;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.event_manager.enums.EventType;
import com.bitdubai.fermat_api.layer.osa_android.file_system.PluginFileSystem;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.error_manager.ErrorManager;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.event_manager.interfaces.EventListener;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.event_manager.interfaces.EventManager;
import com.bitdubai.fermat_dmp_plugin.layer.network_service.wallet_resources.developer.bitdubai.version_1.WalletResourcesNetworkServicePluginRoot;
import com.googlecode.catchexception.CatchException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by natalia on 03/07/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class StartTest extends TestCase {


    @Mock
    private Database mockDatabase;

    @Mock
    private PluginDatabaseSystem mockPluginDatabaseSystem;

    /**
     * DealsWithErrors interface Mocked
     */
    @Mock
    private ErrorManager mockErrorManager;

    /**
     * UsesFileSystem Interface member variables.
     */
    @Mock
    private PluginFileSystem mockPluginFileSystem;

    /**
     * DealWithEvents Iianterface member variables.
     */
    @Mock
    private EventManager mockEventManager;
    @Mock
    private EventListener mockEventListener;

    @Mock
    private LogManager mockLogManager;

    private WalletResourcesNetworkServicePluginRoot testWalletResourcePluginRoot;

    @Before
    public void setUp() throws Exception {
        testWalletResourcePluginRoot = new WalletResourcesNetworkServicePluginRoot();
        testWalletResourcePluginRoot.setPluginFileSystem(mockPluginFileSystem);
        testWalletResourcePluginRoot.setPluginDatabaseSystem(mockPluginDatabaseSystem);
        testWalletResourcePluginRoot.setLogManager(mockLogManager);
        testWalletResourcePluginRoot.setEventManager(mockEventManager);
        testWalletResourcePluginRoot.setErrorManager(mockErrorManager);


        when(mockPluginDatabaseSystem.openDatabase(any(UUID.class), anyString())).thenReturn(mockDatabase);
    }

    @Test
    public void teststart_ThePlugInHasStartedOk_ThrowsCantStartPluginException() throws Exception {
        when(mockEventManager.getNewListener(EventType.BEGUN_WALLET_INSTALLATION)).thenReturn(mockEventListener);
        catchException(testWalletResourcePluginRoot).start();
        assertThat(CatchException.<Exception>caughtException()).isNull();
        assertThat(testWalletResourcePluginRoot.getStatus()).isEqualTo(ServiceStatus.STARTED);
    }


    @Test
    public void getStatusTest()  {


        testWalletResourcePluginRoot.resume();
        assertThat(testWalletResourcePluginRoot.getStatus()).isEqualTo(ServiceStatus.STARTED);

        testWalletResourcePluginRoot.pause();
        assertThat(testWalletResourcePluginRoot.getStatus()).isEqualTo(ServiceStatus.PAUSED);

        testWalletResourcePluginRoot.stop();
        assertThat(testWalletResourcePluginRoot.getStatus()).isEqualTo(ServiceStatus.STOPPED);

    }

}
