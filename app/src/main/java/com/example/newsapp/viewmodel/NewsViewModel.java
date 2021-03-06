package com.example.newsapp.viewmodel;

import android.webkit.WebView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.newsapp.model.Article;
import com.example.newsapp.model.News;
import com.example.newsapp.repository.NewsRepository;
import com.example.newsapp.util.Constants;
import com.example.newsapp.util.NetworkUtil;

import java.util.List;

import javax.inject.Inject;

public class NewsViewModel extends ViewModel {

    private static final String TAG = "NewsViewModel";
    @Inject
    WebView webView;
    @Inject
    NetworkUtil networkUtil;
    private NewsRepository repository;
    private MutableLiveData<List<Article>> articleList = new MutableLiveData<>();
    private MutableLiveData<Boolean> saveNewsSuccess = new MutableLiveData<>();
    private LiveData<NewsResource<News>> newsResourceLiveData;

    @Inject
    public NewsViewModel(NewsRepository newsRepository) {
        this.repository = newsRepository;
    }

    public void init() {
        if (newsResourceLiveData != null) {
            return;
        }
        fetchNewsFromApi();
    }

    private void fetchNewsFromApi() {
        repository.getNewsApi();
        newsResourceLiveData = Transformations.map(repository.getNews(), news -> {
            if (news.getStatus().equals(Constants.STATUS_FAILED)) {
                return NewsResource.error("Could not fetch", null);
            }
            articleList.postValue(news.getArticle());
            return NewsResource.success(news);
        });
    }

    public LiveData<NewsResource<News>> getNewsList() {
        return newsResourceLiveData;
    }

    public boolean getArticleSavedState(int position) {
        return articleList.getValue().get(position).getArticleSaved();
    }

    public LiveData<Boolean> saveNewsItem(int position) {
        boolean isSuccessfullySaved = repository.saveNewsItem(articleList.getValue().get(position));
        saveNewsSuccess.postValue(isSuccessfullySaved);
        if (isSuccessfullySaved) {
            saveWebViewCache(articleList.getValue().get(position).getUrl());
        }
        return saveNewsSuccess;
    }

    public boolean deleteNewsArticle(Article article) {
        int result = repository.deleteNewsArticle(article);
        return result != -1;
    }

    private void saveWebViewCache(String url) {
        if (networkUtil.isNetworkConnected()) {
            webView.loadUrl(url);
        }
    }

}