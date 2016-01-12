package com.bitdubai.reference_wallet.crypto_customer_wallet.common.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitdubai.fermat_android_api.ui.adapters.FermatAdapter;
import com.bitdubai.fermat_android_api.ui.holders.FermatViewHolder;
import com.bitdubai.fermat_cbp_api.all_definition.enums.ClauseType;
import com.bitdubai.fermat_cbp_api.layer.wallet_module.common.interfaces.ClauseInformation;
import com.bitdubai.fermat_cbp_api.layer.wallet_module.common.interfaces.CustomerBrokerNegotiationInformation;
import com.bitdubai.reference_wallet.crypto_customer_wallet.R;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.holders.start_negotiation.AmountToBuyViewHolder;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.holders.start_negotiation.ClauseViewHolder;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.holders.start_negotiation.ExchangeRateViewHolder;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.holders.start_negotiation.FooterViewHolder;
import com.bitdubai.reference_wallet.crypto_customer_wallet.common.holders.start_negotiation.SingleChoiceViewHolder;
import com.bitdubai.reference_wallet.crypto_customer_wallet.fragments.start_negotiation.StartNegotiationActivityFragment;
import com.bitdubai.reference_wallet.crypto_customer_wallet.util.FragmentsCommons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;


public class StartNegotiationAdapter extends FermatAdapter<ClauseInformation, FermatViewHolder> {

    private static final int TYPE_ITEM_SINGLE_CHOICE = 1;
    private static final int TYPE_ITEM_EXCHANGE_RATE = 3;
    private static final int TYPE_ITEM_AMOUNT_TO_BUY = 4;
    private static final int TYPE_FOOTER = 5;

    private CustomerBrokerNegotiationInformation negotiationInformation;
    private StartNegotiationActivityFragment footerListener;
    ClauseViewHolder.Listener clauseListener;


    public StartNegotiationAdapter(Context context, CustomerBrokerNegotiationInformation negotiationInformation) {
        super(context);

        this.negotiationInformation = negotiationInformation;

        dataSet = new ArrayList<>();
        dataSet.addAll(buildListOfItems());
    }

    @Override
    public FermatViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        return createHolder(LayoutInflater.from(context).inflate(getCardViewResource(type), viewGroup, false), type);
    }

    @Override
    protected FermatViewHolder createHolder(View itemView, int type) {
        switch (type) {
            case TYPE_ITEM_SINGLE_CHOICE:
                return new SingleChoiceViewHolder(itemView);

            case TYPE_ITEM_EXCHANGE_RATE:
                return new ExchangeRateViewHolder(itemView);

            case TYPE_ITEM_AMOUNT_TO_BUY:
                return new AmountToBuyViewHolder(itemView);

            case TYPE_FOOTER:
                final FooterViewHolder footerViewHolder = new FooterViewHolder(itemView);
                footerViewHolder.setListener(footerListener);
                return footerViewHolder;

            default:
                throw new IllegalArgumentException("Cant recognise the given value");
        }
    }

    private int getCardViewResource(int type) {
        switch (type) {
            case TYPE_ITEM_SINGLE_CHOICE:
                return R.layout.single_choice_item;
            case TYPE_ITEM_EXCHANGE_RATE:
                return R.layout.exchange_rate_item;
            case TYPE_ITEM_AMOUNT_TO_BUY:
                return R.layout.amount_to_buy_item;
            case TYPE_FOOTER:
                return R.layout.footer_item;
            default:
                throw new NoSuchElementException("Incorrect type value");
        }
    }

    @Override
    protected int getCardViewResource() {
        return 0;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterPosition(position))
            return TYPE_FOOTER;

        ClauseType type = dataSet.get(position).getType();
        switch (type) {
            case CUSTOMER_CURRENCY_QUANTITY:
                return TYPE_ITEM_AMOUNT_TO_BUY;
            case EXCHANGE_RATE:
                return TYPE_ITEM_EXCHANGE_RATE;
            case BROKER_CURRENCY:
                return TYPE_ITEM_SINGLE_CHOICE;
            case CUSTOMER_PAYMENT_METHOD:
                return TYPE_ITEM_SINGLE_CHOICE;
            default:
                throw new NoSuchElementException("Incorrect type value");
        }
    }

    @Override
    public void onBindViewHolder(FermatViewHolder holder, int position) {
        if (!isFooterPosition(position))
            super.onBindViewHolder(holder, position);
    }

    @Override
    protected void bindHolder(FermatViewHolder holder, ClauseInformation clause, int position) {
        final ClauseViewHolder clauseViewHolder = (ClauseViewHolder) holder;
        clauseViewHolder.bindData(negotiationInformation, clause, position);
        clauseViewHolder.getConfirmButton().setVisibility(View.GONE);
        clauseViewHolder.setListener(clauseListener);

        final int clauseNumber = position + 1;
        final int clauseNumberImageRes = FragmentsCommons.getClauseNumberImageRes(clauseNumber);

        switch (clause.getType()) {
            case CUSTOMER_CURRENCY_QUANTITY:
                clauseViewHolder.setViewResources(R.string.ccw_amount_to_buy, clauseNumberImageRes);
                break;
            case EXCHANGE_RATE:
                clauseViewHolder.setViewResources(R.string.exchange_rate_reference, clauseNumberImageRes);
                break;
            case BROKER_CURRENCY:
                clauseViewHolder.setViewResources(R.string.ccw_currency_to_pay, clauseNumberImageRes, R.string.ccw_currency_description);
                break;
            case CUSTOMER_PAYMENT_METHOD:
                clauseViewHolder.setViewResources(R.string.payment_methods_title, clauseNumberImageRes, R.string.payment_method);
                break;
        }
    }

    public void setItem(int position, ClauseInformation clause) {
        dataSet.set(position, clause);
        notifyItemChanged(position);
    }

    public void setFooterListener(StartNegotiationActivityFragment footerListener) {
        this.footerListener = footerListener;
    }

    public void setClauseListener(ClauseViewHolder.Listener clauseListener) {
        this.clauseListener = clauseListener;
    }

    private List<ClauseInformation> buildListOfItems() {
        final int TOTAL_STEPS = 4;

        final Collection<ClauseInformation> values = negotiationInformation.getClauses().values();
        final List<ClauseInformation> list = new ArrayList<>(TOTAL_STEPS);

        for (ClauseInformation value : values)
            switch (value.getType()) {
                case CUSTOMER_CURRENCY_QUANTITY:
                    list.set(0, value);
                    break;
                case EXCHANGE_RATE:
                    list.set(1, value);
                    break;
                case BROKER_CURRENCY:
                    list.set(2, value);
                    break;
                case CUSTOMER_PAYMENT_METHOD:
                    list.set(3, value);
                    break;
            }

        return list;
    }

    private boolean isFooterPosition(int position) {
        return position == getItemCount() - 1;
    }
}
