package com.bitdubai.fermat_ccp_api.all_definition.enums;

import com.bitdubai.fermat_api.layer.all_definition.enums.interfaces.FermatEnum;
import com.bitdubai.fermat_api.layer.all_definition.exceptions.InvalidParameterException;

/**
 * Created by natalia on 18/02/16.
 */
public enum SubAppsPublicKeys implements FermatEnum {
    //TODO: MUY PROVISORIO Para usar en el bradcaster de notificaciones
    CCP_COMMUNITY               ("public_key_intra_user_commmunity"),
    CCP_IDENTITY               ("public_key_ccp_intra_user_identity");

    private String code;

    SubAppsPublicKeys(String code){
        this.code = code;
    }

    public static SubAppsPublicKeys getByCode(String code) throws InvalidParameterException {

        switch (code){

            case "public_key_intra_user_commmunity": return CCP_COMMUNITY               ;
            case "public_key_intra_user_identity": return CCP_IDENTITY     ;


            default:
                throw new InvalidParameterException(
                        "Code Received: " + code,
                        "This code is not valid for the RequestProtocolState enum"
                );
        }
    }

    @Override
    public String getCode(){
        return this.code;
    }

}

