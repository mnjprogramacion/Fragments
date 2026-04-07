package com.example.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.StackView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewsListFragment extends Fragment {

    private List<NewsItem> newsList = new ArrayList<>();
    private List<NewsItem> filteredList = new ArrayList<>();
    private NewsBaseAdapter baseAdapter;
    private FrameLayout container;
    private String currentQuery = "";

    private static final String CSV_BASE_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRr3-LdTANTCbj0ojujZRxBttAdkwjLtwIIY7XT4SMCDZoBLSW8NqAQPS7BfuO2nn5AByiqS31x6kaJ/pub?output=csv&gid=";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup containerParent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, containerParent, false);

        container = view.findViewById(R.id.container);
        baseAdapter = new NewsBaseAdapter(filteredList);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_news_list);
        setupToolbar(toolbar);

        Spinner spinner = view.findViewById(R.id.spinnerViewType);
        String[] viewTypes = {"ListView", "GridView", "StackView", "Gallery"};
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, viewTypes));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                showAdapterView(viewTypes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        downloadCsv();
        return view;
    }

    private boolean isDualPane() {
        return getActivity() != null && getActivity().findViewById(R.id.detailContainer) != null;
    }

    @SuppressWarnings("deprecation")
    private void showAdapterView(String type) {
        container.removeAllViews();
        AdapterView adapterView;
        int layoutRes;

        switch (type) {
            case "GridView":
                GridView grid = new GridView(requireContext());
                grid.setNumColumns(2);
                adapterView = grid;
                layoutRes = R.layout.item_news;
                break;
            case "StackView":
                adapterView = new StackView(requireContext());
                layoutRes = R.layout.item_news_stack;
                break;
            case "Gallery":
                Gallery gallery = new Gallery(requireContext());
                gallery.setSpacing(8);
                adapterView = gallery;
                layoutRes = R.layout.item_news_stack;
                break;
            default:
                adapterView = new ListView(requireContext());
                layoutRes = R.layout.item_news;
                break;
        }

        NewsAdapterWrapper wrapper = new NewsAdapterWrapper(requireContext(), baseAdapter, layoutRes);

        adapterView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        adapterView.setAdapter(wrapper);
        adapterView.setOnItemClickListener((parent, view, position, id) -> {
            NewsItem item = filteredList.get(position);
            if (isDualPane()) {
                NewsDetailFragment detail = NewsDetailFragment.newInstance(item);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailContainer, detail)
                        .commit();
                View placeholder = requireActivity().findViewById(R.id.txtPlaceholder);
                if (placeholder != null) placeholder.setVisibility(View.GONE);
            } else {
                Intent intent = new Intent(requireContext(), DetailActivity.class);
                intent.putExtra("news", item);
                startActivity(intent);
            }
        });

        container.addView(adapterView);
    }

    private void downloadCsv() {
        new Thread(() -> {
            try {
                String gid = requireContext().getString(R.string.csv_gid);
                URL url = new URL(CSV_BASE_URL + gid);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                boolean first = true;
                List<NewsItem> items = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    if (first) { first = false; continue; }
                    String[] cols = parseCsvLine(line);
                    if (cols.length >= 7) {
                        int importancia = 0;
                        try { importancia = Integer.parseInt(cols[6].trim()); } catch (NumberFormatException ignored) {}
                        items.add(new NewsItem(
                                cols[0].trim(), cols[1].trim(), cols[2].trim(),
                                cols[3].trim(), cols[4].trim(), cols[5].trim(), importancia));
                    }
                }
                reader.close();

                SharedPreferences prefs = requireContext().getSharedPreferences("news_prefs", Context.MODE_PRIVATE);
                prefs.edit().putInt("last_news_count", items.size()).apply();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        newsList.clear();
                        newsList.addAll(items);
                        Collections.sort(newsList, (a, b) -> b.fecha.compareTo(a.fecha));
                        applyFilter();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupToolbar(Toolbar toolbar) {
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                applyFilter();
                return true;
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sort_fecha) {
                Collections.sort(newsList, (a, b) -> b.fecha.compareTo(a.fecha));
                applyFilter();
                return true;
            } else if (id == R.id.sort_importancia) {
                Collections.sort(newsList, (a, b) -> Integer.compare(b.importancia, a.importancia));
                applyFilter();
                return true;
            } else if (id == R.id.action_interval) {
                showIntervalDialog();
                return true;
            }
            return false;
        });
    }

    private void applyFilter() {
        filteredList.clear();
        String query = currentQuery.toLowerCase();
        for (NewsItem item : newsList) {
            if (query.isEmpty() || item.titulo.toLowerCase().contains(query)
                    || item.descripcion.toLowerCase().contains(query)) {
                filteredList.add(item);
            }
        }
        baseAdapter.notifyDataSetChanged();
        View view = getView();
        if (view != null) {
            Spinner sp = view.findViewById(R.id.spinnerViewType);
            showAdapterView(sp.getSelectedItem().toString());
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private void showIntervalDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        String[] options = {
                getString(R.string.interval_15),
                getString(R.string.interval_30),
                getString(R.string.interval_60)
        };
        int[] minutes = {15, 30, 60};

        SharedPreferences prefs = requireContext().getSharedPreferences("news_prefs", Context.MODE_PRIVATE);
        int current = prefs.getInt("check_interval", -1);
        int checkedIndex = -1;
        for (int i = 0; i < minutes.length; i++) {
            if (minutes[i] == current) { checkedIndex = i; break; }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.interval_title))
                .setSingleChoiceItems(options, checkedIndex, (dialog, which) -> {
                    scheduleNewsCheck(minutes[which]);
                    dialog.dismiss();
                })
                .show();
    }

    private void scheduleNewsCheck(int minutes) {
        SharedPreferences prefs = requireContext().getSharedPreferences("news_prefs", 0);
        prefs.edit().putInt("check_interval", minutes).apply();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                NewsCheckWorker.class, minutes, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(requireContext())
                .enqueueUniquePeriodicWork("news_check",
                        ExistingPeriodicWorkPolicy.REPLACE, request);
    }
}
