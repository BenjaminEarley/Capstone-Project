package com.benjaminearley.mysubs;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class StoryDetailFragment extends Fragment {

    public static final String ARG_ITEM_LINK = "item_url";
    static final String DETAIL_URI = "uri";
    private String link;
    private Uri uri;
    private ProgressBar progressBar;

    public StoryDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_LINK)) {

            uri = getArguments().getParcelable(DETAIL_URI);
            link = getArguments().getString(ARG_ITEM_LINK);

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.story_detail, container, false);

        if ((link != null && !link.isEmpty()) || (uri != null && !uri.toString().isEmpty())) {

            WebView webView = (WebView) rootView.findViewById(R.id.webview);
            progressBar = (ProgressBar) getActivity().findViewById(R.id.toolbar_progress_bar);
            if (progressBar != null) {
                progressBar.setProgress(0);
            }

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setSupportZoom(true);
            webSettings.setDisplayZoomControls(false);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });

            webView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onProgressChanged(WebView view, int progress) {

                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                        if (progress >= 100) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public boolean onJsConfirm(
                        WebView view,
                        String url,
                        String message,
                        final JsResult result) {

                    showAlertDialog(message, result);

                    return true;
                }

                @Override
                public boolean onJsAlert(
                        WebView view,
                        String url,
                        String message,
                        final JsResult result) {

                    showAlertDialog(message, result);

                    return true;
                }

                public void showAlertDialog(String message, final JsResult result) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            getContext());

                    builder.setMessage(message).setPositiveButton(
                            android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            }).setCancelable(false).create().show();
                }
            });

            if (link != null) {
                webView.loadUrl(getContext().getString(R.string.reddit_url_webview) + link);
            } else {
                webView.loadUrl(getContext().getString(R.string.reddit_url_webview) + uri.toString());
            }
        } else {
            Toast.makeText(getContext(), R.string.toast_web_error, Toast.LENGTH_LONG).show();
        }

        return rootView;
    }
}
