package net.darkion.swipespinner;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.math.MathUtils;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import static net.darkion.swipespinner.MainActivity.ItemDrawable.INIT_ALPHA;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView cover = findViewById(R.id.cover);

        RecyclerView date = findViewById(R.id.date);
        RecyclerView month = findViewById(R.id.month);
        RecyclerView year = findViewById(R.id.year);
        RecyclerView emoji = findViewById(R.id.emoji);

        initSnapperAndAesthetics(cover, date, month, year, emoji);

        cover.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        cover.setAdapter(getCoverAdapter());


        date.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        month.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        year.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        emoji.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        date.setAdapter(getDateAdapter());
        month.setAdapter(getMonthsAdapter());
        year.setAdapter(getYearsAdapter());
        emoji.setAdapter(getEmojisAdapter());
    }

    private void initSnapperAndAesthetics(RecyclerView... recyclerViews) {
        for (final RecyclerView recyclerView : recyclerViews) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    SwipeSpinnerHelper swipeSpinnerHelper = SwipeSpinnerHelper.bindRecyclerView(recyclerView);
                    final ItemDrawable itemDrawable = new ItemDrawable(swipeSpinnerHelper);
                    SwipeSpinnerHelper.ScrollCallbacks scrollCallbacks = new SwipeSpinnerHelper.ScrollCallbacks() {
                        @Override
                        public void onScrolled(float directedInterpolationFraction) {
                            Drawable otherDrawable, currentDirection;
                            otherDrawable = directedInterpolationFraction > 0 ? itemDrawable.indicators.getDrawable(0) : itemDrawable.indicators.getDrawable(1);
                            currentDirection = directedInterpolationFraction > 0 ? itemDrawable.indicators.getDrawable(1) : itemDrawable.indicators.getDrawable(0);

                            int currentDirectionAlpha = 255;

                            currentDirection.setAlpha(currentDirectionAlpha);
                            otherDrawable.setAlpha(MathUtils.clamp(255 - currentDirectionAlpha, INIT_ALPHA, 255));
                            itemDrawable.indicators.invalidateSelf();
                        }

                        @Override
                        public void onResetScroll() {
                            itemDrawable.resetIndicators();
                        }
                    };
                    swipeSpinnerHelper.setScrollCallbacks(scrollCallbacks);
                }
            });

        }
    }

    class ItemDrawable {
        GradientDrawable gradientDrawable;
        LayerDrawable indicators, finalDrawable;
        final static int INIT_ALPHA = 255 / 4;

        public void resetIndicators() {
            indicators.getDrawable(0).setAlpha(INIT_ALPHA);
            indicators.getDrawable(1).setAlpha(INIT_ALPHA);
            indicators.invalidateSelf();
        }

        ItemDrawable(SwipeSpinnerHelper swipeSpinnerHelper) {
            View v = swipeSpinnerHelper.getRecyclerView();
            Drawable[] drawables = new Drawable[2];
            gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(0xff333333);
            gradientDrawable.setCornerRadius(v.getHeight() / 2);
            drawables[0] = gradientDrawable;

            indicators = (LayerDrawable) AppCompatResources.getDrawable(getApplicationContext(), swipeSpinnerHelper.isVertical() ? R.drawable.arrows : R.drawable.arrows_horizontal).mutate();
            drawables[1] = indicators;

            finalDrawable = new LayerDrawable(drawables);
            v.setBackground(finalDrawable);

            resetIndicators();
        }
    }


    class DataAdapter extends RecyclerView.Adapter<DataAdapter.TextViewHolder> {

        ArrayList<String> mData;

        DataAdapter(ArrayList<String> mData) {
            this.mData = mData;
        }

        class TextViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;

            TextViewHolder(View v) {
                super(v);
                mTextView = v.findViewById(R.id.text);
            }
        }


        @NonNull
        @Override
        public TextViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new TextViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
            holder.mTextView.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    class CoverAdapter extends RecyclerView.Adapter<CoverAdapter.IconViewHolder> {

        ArrayList<Integer> mData;

        CoverAdapter() {
            this.mData = new ArrayList<>(Arrays.asList(0xffD500F9,
                    0xffFF1744,
                    0xff651FFF,
                    0xff2979FF,
                    0xff40C4FF,
                    0xff18FFFF,
                    0xff00E676,
                    0xff76FF03,
                    0xffFFFF00,
                    0xffFF9100,
                    0xffFF3D00
            ));
        }

        class IconViewHolder extends RecyclerView.ViewHolder {
            AppCompatImageView mIcon;

            IconViewHolder(View v) {
                super(v);
                mIcon = v.findViewById(R.id.icon);
            }
        }


        @NonNull
        @Override
        public IconViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new IconViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.horizontal_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            ImageViewCompat.setImageTintList(holder.mIcon, ColorStateList.valueOf(mData.get(position)));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private CoverAdapter getCoverAdapter() {
        return new CoverAdapter();
    }

    private DataAdapter getEmojisAdapter() {
        ArrayList<String> data = new ArrayList<>(Arrays.asList("😀",
                "😁",
                "😂",
                "🤣",
                "😃",
                "😄",
                "😅",
                "😆",
                "😉",
                "😊",
                "😋",
                "😎",
                "😍",
                "😘",
                "🥰",
                "😗",
                "😙",
                "😚",
                "☺️",
                "🙂",
                "🤗",
                "🤩",
                "🤔",
                "🤨",
                "😐",
                "😑",
                "😶",
                "🙄",
                "😏",
                "😣",
                "😥",
                "😮",
                "🤐",
                "😯",
                "😪",
                "😫",
                "😴",
                "😌",
                "😛",
                "😜",
                "😝",
                "🤤",
                "😒",
                "😓",
                "😔",
                "😕",
                "🙃",
                "🤑",
                "😲",
                "☹️",
                "🙁",
                "😖",
                "😞",
                "😟",
                "😤",
                "😢",
                "😭",
                "😦",
                "😧",
                "😨",
                "😩",
                "🤯",
                "😬",
                "😰",
                "😱",
                "🥵",
                "🥶",
                "😳",
                "🤪",
                "😵",
                "😡",
                "😠",
                "🤬",
                "😷",
                "🤒",
                "🤕",
                "🤢",
                "🤮",
                "🤧",
                "😇",
                "🤠"));


        return new DataAdapter(data);
    }

    private DataAdapter getDateAdapter() {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            data.add(String.valueOf(i));
        }
        return new DataAdapter(data);
    }


    private DataAdapter getMonthsAdapter() {
        ArrayList<String> data = new ArrayList<>();
        data.add("January");
        data.add("February");
        data.add("March");
        data.add("April");
        data.add("May");
        data.add("June");
        data.add("July");
        data.add("August");
        data.add("September");
        data.add("October");
        data.add("November");
        data.add("December");
        return new DataAdapter(data);
    }

    private DataAdapter getYearsAdapter() {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 1940; i < 2019; i++) {
            data.add(String.valueOf(i));
        }
        return new DataAdapter(data);
    }

}
