/*
 * Copyright (C) 2015 Willi Ye
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.grarak.kerneladiutor.fragments.kernel;

import android.content.Context;
import android.os.Bundle;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.PathReaderFragment;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.fragments.ViewPagerFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.IO;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by willi on 11.04.15.
 */
public class IOFragment extends ViewPagerFragment implements Constants {

    private static WeakReference < IOFragment > ioFragment;
    private IOPart ioPart;
    private SchedulerPart schedulerPart;
    private IO.StorageType storageType;

    @Override
    public void preInit(Bundle savedInstanceState) {
        super.preInit(savedInstanceState);
        showTabs(false);
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        ioFragment = new WeakReference < IOFragment > (this);

        allowSwipe(false);
        addFragment(new ViewPagerItem(ioPart == null ? ioPart = new IOPart() : ioPart, null));
        addFragment(new ViewPagerItem(schedulerPart == null ? schedulerPart = new SchedulerPart() : schedulerPart, null));
    }

    @Override
    public void onSwipe(int page) {
        super.onSwipe(page);
        allowSwipe(page == 1);
    }

    @Override
    public boolean onBackPressed() {
        if (getCurrentPage() == 1) {
            setCurrentItem(0);
            return true;
        }
        return false;
    }

    public static class IOPart extends RecyclerViewFragment implements PopupCardView.DPopupCard.OnDPopupCardListener,
        CardViewItem.DCardView.OnDCardListener, SwitchCardView.DSwitchCard.OnDSwitchCardListener {

            private final List < String > readheads = new ArrayList < > ();

            private final List < String > list = new ArrayList < > ();

            private PopupCardView.DPopupCard mInternalSchedulerCard, mExternalSchedulerCard;

            private CardViewItem.DCardView mInternalTunableCard, mExternalTunableCard;

            private PopupCardView.DPopupCard mInternalReadAheadCard, mExternalReadAheadCard, mIOAffinityCard;

            private SwitchCardView.DSwitchCard mRotationalCard, mIOStatsCard, mIORandomCard;

            @Override
            public String getClassName() {
                return IOFragment.class.getSimpleName();
            }

            @Override
            public void init(Bundle savedInstanceState) {
                super.init(savedInstanceState);

                readheads.clear();

                internalStorageInit();
                if (IO.hasExternalStorage()) externalStorageInit();
                if (IO.hasIOStats()) IOStatsInit();
                if (IO.hasIOAffinity()) IOAffintyInit();
            }

            private void internalStorageInit() {
                DDivider mInternalStorageDivider = new DDivider();
                mInternalStorageDivider.setText(getString(R.string.internal_storage));

                addView(mInternalStorageDivider);

                mInternalSchedulerCard = new PopupCardView.DPopupCard(IO.getSchedulers(IO.StorageType.INTERNAL));
                mInternalSchedulerCard.setTitle(getString(R.string.scheduler));
                mInternalSchedulerCard.setDescription(getString(R.string.scheduler_summary));
                mInternalSchedulerCard.setItem(IO.getScheduler(IO.StorageType.INTERNAL));
                mInternalSchedulerCard.setOnDPopupCardListener(this);

                addView(mInternalSchedulerCard);

                mInternalTunableCard = new CardViewItem.DCardView();
                mInternalTunableCard.setTitle(getString(R.string.scheduler_tunable));
                mInternalTunableCard.setDescription(getString(R.string.scheduler_tunable_summary));
                mInternalTunableCard.setOnDCardListener(this);

                addView(mInternalTunableCard);

                for (int i = 0; i < 256; i++)
                    readheads.add((i * 64 + 64) + getString(R.string.kb));

                mInternalReadAheadCard = new PopupCardView.DPopupCard(readheads);
                mInternalReadAheadCard.setTitle(getString(R.string.read_ahead));
                mInternalReadAheadCard.setDescription(getString(R.string.read_ahead_summary));
                mInternalReadAheadCard.setItem(IO.getReadahead(IO.StorageType.INTERNAL) + getString(R.string.kb));
                mInternalReadAheadCard.setOnDPopupCardListener(this);

                addView(mInternalReadAheadCard);
            }

            private void externalStorageInit() {
                DDivider mExternalStorageDivider = new DDivider();
                mExternalStorageDivider.setText(getString(R.string.external_storage));

                addView(mExternalStorageDivider);

                mExternalSchedulerCard = new PopupCardView.DPopupCard(IO.getSchedulers(IO.StorageType.EXTERNAL));
                mExternalSchedulerCard.setDescription(getString(R.string.scheduler));
                mExternalSchedulerCard.setItem(IO.getScheduler(IO.StorageType.EXTERNAL));
                mExternalSchedulerCard.setOnDPopupCardListener(this);

                addView(mExternalSchedulerCard);

                mExternalTunableCard = new CardViewItem.DCardView();
                mExternalTunableCard.setDescription(getString(R.string.scheduler_tunable));
                mExternalTunableCard.setOnDCardListener(this);

                addView(mExternalTunableCard);

                mExternalReadAheadCard = new PopupCardView.DPopupCard(readheads);
                mExternalReadAheadCard.setDescription(getString(R.string.read_ahead));
                mExternalReadAheadCard.setItem(IO.getReadahead(IO.StorageType.EXTERNAL) + getString(R.string.kb));
                mExternalReadAheadCard.setOnDPopupCardListener(this);

                addView(mExternalReadAheadCard);
            }

            private void RotationalInit() {
                mRotationalCard = new SwitchCardView.DSwitchCard();
                mRotationalCard.setTitle(getString(R.string.rotational));
                mRotationalCard.setDescription(getString(R.string.rotational_summary));
                mRotationalCard.setChecked(IO.isRotationalActive());
                mRotationalCard.setOnDSwitchCardListener(this);

                addView(mRotationalCard);
            }

            private void IORandomInit() {
                mIORandomCard = new SwitchCardView.DSwitchCard();
                mIORandomCard.setTitle(getString(R.string.iorandom));
                mIORandomCard.setDescription(getString(R.string.iorandom_summary));
                mIORandomCard.setChecked(IO.isIORandomActive());
                mIORandomCard.setOnDSwitchCardListener(this);

                addView(mIORandomCard);
            }

            private void IOStatsInit() {
                mIOStatsCard = new SwitchCardView.DSwitchCard();
                mIOStatsCard.setTitle(getString(R.string.iostats));
                mIOStatsCard.setDescription(getString(R.string.iostats_summary));
                mIOStatsCard.setChecked(IO.isIOStatsActive());
                mIOStatsCard.setOnDSwitchCardListener(this);

                addView(mIOStatsCard);
            }

            private void IOAffintyInit() {
                List < String > list = new ArrayList < > ();
                list.add("0 " + getString(R.string.disabled2));
                list.add("1 " + getString(R.string.enabled2));
                list.add("2 " + getString(R.string.aggressive2));

                mIOAffinityCard = new PopupCardView.DPopupCard(list);
                mIOAffinityCard.setTitle(getString(R.string.ioaffitiny));
                mIOAffinityCard.setDescription(getString(R.string.ioraffinity_summary));
                mIOAffinityCard.setItem(IO.getIOAffinity());
                mIOAffinityCard.setOnDPopupCardListener(this);

                addView(mIOAffinityCard);
            }

            @Override
            public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
                if (dPopupCard == mInternalSchedulerCard)
                    IO.setScheduler(IO.StorageType.INTERNAL, IO.getSchedulers(IO.StorageType.INTERNAL)
                        .get(position), getActivity());
                else if (dPopupCard == mExternalSchedulerCard)
                    IO.setScheduler(IO.StorageType.EXTERNAL, IO.getSchedulers(IO.StorageType.EXTERNAL)
                        .get(position), getActivity());
                else if (dPopupCard == mInternalReadAheadCard)
                    IO.setReadahead(IO.StorageType.INTERNAL, Utils.stringToInt(readheads.get(position)
                        .replace(getString(R.string.kb), "")), getActivity());
                else if (dPopupCard == mExternalReadAheadCard)
                    IO.setReadahead(IO.StorageType.EXTERNAL, Utils.stringToInt(readheads.get(position)
                        .replace(getString(R.string.kb), "")), getActivity());
                else if (dPopupCard == mIOAffinityCard)
                    IO.setIOAffinity(position, getActivity());
            }

            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                ioFragment.get().storageType = dCardView == mInternalTunableCard ? IO.StorageType.INTERNAL : IO.StorageType.EXTERNAL;
                ioFragment.get().schedulerPart.reload();
                ioFragment.get().setCurrentItem(1);
            }

            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                if (dSwitchCard == mRotationalCard)
                    IO.activaterotational(checked, getActivity());
                else if (dSwitchCard == mIORandomCard)
                    IO.activateIORandom(checked, getActivity());
                else if (dSwitchCard == mIOStatsCard)
                    IO.activateIOstats(checked, getActivity());
            }
        }

    public static class SchedulerPart extends PathReaderFragment {

        @Override
        public String getName() {
            return IO.getScheduler(ioFragment.get().storageType == IO.StorageType.INTERNAL ? IO.StorageType.INTERNAL :
                IO.StorageType.EXTERNAL);
        }

        @Override
        public String getPath() {
            return ioFragment.get().storageType == IO.StorageType.INTERNAL ? IO_INTERNAL_SCHEDULER_TUNABLE :
                IO_EXTERNAL_SCHEDULER_TUNABLE;
        }

        @Override
        public PATH_TYPE getType() {
            return PATH_TYPE.IO;
        }

        @Override
        public String getError(Context context) {
            return context.getString(R.string.not_tunable, IO.getScheduler(ioFragment.get().storageType == IO.StorageType.INTERNAL ?
                IO.StorageType.INTERNAL : IO.StorageType.EXTERNAL));
        }

    }

}
