package tr.xip.wanikani.cards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;

import tr.xip.wanikani.BroadcastIntents;
import tr.xip.wanikani.R;
import tr.xip.wanikani.adapters.CriticalItemsAdapter;
import tr.xip.wanikani.api.WaniKaniApi;
import tr.xip.wanikani.api.response.CriticalItemsList;
import tr.xip.wanikani.utils.Utils;

/**
 * Created by xihsa_000 on 3/13/14.
 */
public class CriticalItemsCard extends Fragment {

    View rootView;

    WaniKaniApi api;
    Utils utils;

    Context mContext;

    TextView mCardTitle;
    ListView mCriticalItemsList;

    CriticalItemsAdapter mCriticalItemsAdapter;

    ViewFlipper mViewFlipper;
    ViewFlipper mConnectionViewFlipper;

    private BroadcastReceiver mDoLoad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            new LoadTask().execute();
        }
    };

    @Override
    public void onCreate(Bundle state) {
        api = new WaniKaniApi(getActivity());
        utils = new Utils(getActivity());
        super.onCreate(state);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDoLoad,
                new IntentFilter(BroadcastIntents.SYNC()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_critical_items, null);

        mCardTitle = (TextView) rootView.findViewById(R.id.card_critical_items_title);
        mCriticalItemsList = (ListView) rootView.findViewById(R.id.card_critical_items_list);

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.card_critical_items_view_flipper);
        mViewFlipper.setInAnimation(getActivity(), R.anim.abc_fade_in);
        mViewFlipper.setOutAnimation(getActivity(), R.anim.abc_fade_out);

        mConnectionViewFlipper = (ViewFlipper) rootView.findViewById(R.id.card_critical_items_connection_view_flipper);
        mConnectionViewFlipper.setInAnimation(getActivity(), R.anim.abc_fade_in);
        mConnectionViewFlipper.setOutAnimation(getActivity(), R.anim.abc_fade_out);

        return rootView;
    }

    public int setCriticalItemsHeightBasedOnListView(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return (int) pxFromDp(550);
        } else {

            int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                if (listItem instanceof ViewGroup) {
                    listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            totalHeight += mCardTitle.getMeasuredHeight();
            totalHeight += pxFromDp(16); // Add the paddings as well

            return totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        }
    }

    private float pxFromDp(float dp) {
        return dp * mContext.getResources().getDisplayMetrics().density;
    }

    private class LoadTask extends AsyncTask<String, Void, List<CriticalItemsList.CriticalItem>> {

        @Override
        protected List<CriticalItemsList.CriticalItem> doInBackground(String... strings) {
            List<CriticalItemsList.CriticalItem> list = null;

            try {
                list = api.getCriticalItemsList(85); // TODO - Revert to 75
            } catch (Exception e) {
                e.printStackTrace();
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<CriticalItemsList.CriticalItem> result) {
            int height;

            if (result != null) {
                mCriticalItemsAdapter = new CriticalItemsAdapter(mContext,
                        R.layout.item_critical, result);
                mCriticalItemsList.setAdapter(mCriticalItemsAdapter);
                if (mConnectionViewFlipper.getDisplayedChild() == 1) {
                    mConnectionViewFlipper.showPrevious();
                }

                height = setCriticalItemsHeightBasedOnListView(mCriticalItemsList);
            } else {
                height = (int) pxFromDp(158);

                if (mConnectionViewFlipper.getDisplayedChild() == 0) {
                    mConnectionViewFlipper.showNext();
                }
            }

            if (mViewFlipper.getDisplayedChild() == 0) {
                mViewFlipper.showNext();
            }

            Intent intent = new Intent(BroadcastIntents.FINISHED_SYNC_CRITICAL_ITEMS_CARD());
            intent.putExtra("height", height);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

}