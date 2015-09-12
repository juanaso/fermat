package unit.com.bitdubai.fermat_dmp_plugin.layer.middleware.wallet_contacts.developer.bitdubai.version_1.database.WalletContactsMiddlewareDeveloperDatabaseFactory;

import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabase;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperObjectFactory;
import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_dmp_plugin.layer.middleware.wallet_contacts.developer.bitdubai.version_1.database.WalletContactsMiddlewareDeveloperDatabaseFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by natalia on 10/09/15.
 */

@RunWith(MockitoJUnitRunner.class)
public class GetDatabaseTableListTest {
    @Mock
    private Database mockDatabase;

    @Mock
    private DeveloperObjectFactory mockDeveloperObjectFactory;

    @Mock
    private PluginDatabaseSystem mockPluginDatabaseSystem;

    @Mock
    private DeveloperObjectFactory developerObjectFactory;

    @Mock
    private DeveloperDatabase developerDatabase;

    private WalletContactsMiddlewareDeveloperDatabaseFactory developerDatabaseFactory;


    @Test
    public void getDatabaseTableListTest_GetOk() throws Exception {

        UUID testOwnerId = UUID.randomUUID();

        when(mockPluginDatabaseSystem.openDatabase(any(UUID.class), anyString())).thenReturn(mockDatabase);

        developerDatabaseFactory = new WalletContactsMiddlewareDeveloperDatabaseFactory(mockPluginDatabaseSystem, testOwnerId);

        developerDatabaseFactory.initializeDatabase();

        assertThat(developerDatabaseFactory.getDatabaseTableList(mockDeveloperObjectFactory)).isInstanceOf(List.class);
    }
}

