package com.example.newsapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.R;
import com.example.newsapp.adapter.NewsListAdapter;
import com.example.newsapp.model.Article;
import com.example.newsapp.model.News;
import com.example.newsapp.util.Constants;
import com.example.newsapp.util.NetworkUtil;
import com.example.newsapp.viewmodel.NewsResource;
import com.example.newsapp.viewmodel.NewsViewModel;
import com.example.newsapp.viewmodel.ViewModelProviderFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class NewsActivity extends DaggerAppCompatActivity implements NewsListAdapter.OnNewsClickListener {

    @Inject
    ViewModelProviderFactory providerFactory;
    private NewsViewModel viewModel;
    private ProgressBar progressBar;

    @Inject
    NetworkUtil networkUtil;

    private RecyclerView recyclerView;
    private List<Article> articles = new ArrayList<>();
    private NewsListAdapter adapter;
    private TextView newsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        progressBar = findViewById(R.id.progress_bar);
        newsError = findViewById(R.id.news_error);
        initViewModel();
        getNewsList();
        setupRecyclerView();
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this, providerFactory).get(NewsViewModel.class);
        viewModel.init();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void getNewsList() {
        showProgressBar(true);
        viewModel.getNewsList()
                .observe(this, new Observer<NewsResource<News>>() {
                    @Override
                    public void onChanged(NewsResource<News> newsNewsResource) {
                        switch (newsNewsResource.status) {
                            case SUCCESS:
                                showProgressBar(false);
                                if (newsNewsResource.data != null && newsNewsResource.data.getStatus().equals(Constants.STATUS_OK)) {
                                    newsError.setVisibility(View.GONE);
                                    if (!articles.isEmpty()) {
                                        articles.clear();
                                    }
                                    articles = newsNewsResource.data.getArticle();
                                    if (articles.size() != 0) {
                                        adapter = new NewsListAdapter(articles, NewsActivity.this, NewsActivity.this);
                                        recyclerView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                    } else newsError.setVisibility(View.VISIBLE);
                                } else {
                                    newsError.setVisibility(View.VISIBLE);
                                    Toast.makeText(NewsActivity.this, "Failed to fetch", Toast.LENGTH_LONG).show();
                                }
                                break;
                            case ERROR:
                                showProgressBar(false);
                                newsError.setVisibility(View.VISIBLE);
                                Toast.makeText(NewsActivity.this, "Failed to fetch", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
    }

    private void showProgressBar(boolean isVisible) {
        progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onNewsClick(int position) {
        Intent newsDetailsIntent = NewsDetailsActivity.newIntent(this, articles.get(position).getSource().getName(), articles.get(position).getUrl());
        startActivity(newsDetailsIntent);
    }

    @Override
    public void onSaveClick(int position) {
        boolean savedState = viewModel.getArticleSavedState(position);
        if (!savedState) {
            viewModel.saveNewsItem(position).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean isSuccessfullySaved) {
                    if (isSuccessfullySaved) {
                        Toast.makeText(getBaseContext(), "News Article Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(), "Failed to Save News Article", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            articles.get(position).setArticleSaved(true);
            adapter.notifyItemChanged(position);
        } else {
            if (viewModel.deleteNewsArticle(articles.get(position))) {
                if (networkUtil.isNetworkConnected()) {
                    articles.get(position).setArticleSaved(false);
                    adapter.notifyItemChanged(position);
                } else {
                    articles.remove(position);
                    adapter.notifyItemRemoved(position);
                }
                if (articles.size() == 0) {
                    newsError.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(getBaseContext(), "News Article Successfully deleted from local storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}