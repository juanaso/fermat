package com.bitdubai.reference_niche_wallet.bitcoin_wallet.fragments.wallet_final_version;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bitdubai.android_fermat_ccp_wallet_bitcoin.R;
import com.bitdubai.fermat_android_api.layer.definition.wallet.FermatWalletFragment;
import com.bitdubai.fermat_api.layer.all_definition.enums.UISource;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Activities;
import com.bitdubai.fermat_ccp_api.layer.module.intra_user.exceptions.CantGetActiveLoginIdentityException;
import com.bitdubai.fermat_ccp_api.layer.module.intra_user.interfaces.IntraUserModuleManager;
import com.bitdubai.fermat_ccp_api.layer.wallet_module.crypto_wallet.exceptions.CantGetCryptoWalletException;
import com.bitdubai.fermat_ccp_api.layer.wallet_module.crypto_wallet.exceptions.CantListCryptoWalletIntraUserIdentityException;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.UnexpectedUIExceptionSeverity;
import com.bitdubai.reference_niche_wallet.bitcoin_wallet.session.ReferenceWalletSession;

/**
 * Created by mati on 2015.11.25..
 */
public class NoIdentityFragment extends FermatWalletFragment {


    /**
     * Plaform reference
     */
    private ReferenceWalletSession referenceWalletSession;
    private IntraUserModuleManager intraUserModuleManager;

    /**
     * UI
     */
    private View rootView;


    public static NoIdentityFragment newInstance() {
        return new NoIdentityFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            referenceWalletSession = (ReferenceWalletSession) walletSession;
            intraUserModuleManager = referenceWalletSession.getIntraUserModuleManager();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        try {
            rootView = inflater.inflate(R.layout.no_odentity_main, container, false);
            setUpUI();
            return rootView;
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Oooops! recovering from system error", Toast.LENGTH_SHORT).show();
            referenceWalletSession.getErrorManager().reportUnexpectedUIException(UISource.VIEW, UnexpectedUIExceptionSeverity.CRASH, e);
        }

        return null;
    }


    private void setUpUI() {
        ((Button) rootView.findViewById(R.id.btn_create)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Comming soon",Toast.LENGTH_SHORT).show();
                try {
                    changeActivity(Activities.CWP_INTRA_USER_ACTIVITY);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        try {
//            if (referenceWalletSession.getModuleManager().getCryptoWallet().getAllIntraWalletUsersFromCurrentDeviceUser().size()>0) {
//                changeActivity(Activities.CWP_WALLET_RUNTIME_WALLET_BASIC_WALLET_BITDUBAI_VERSION_1_MAIN);
//            }
//        } catch (CantListCryptoWalletIntraUserIdentityException e) {
//            e.printStackTrace();
//        } catch (CantGetCryptoWalletException e) {
//            e.printStackTrace();
//        }
    }

}
