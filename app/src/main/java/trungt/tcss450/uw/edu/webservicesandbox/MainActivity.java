package trungt.tcss450.uw.edu.webservicesandbox;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    // fields added
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private Button mButton;
    private AsyncTask<String, Integer, String> mTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textView);
        mProgressBar = findViewById(R.id.progressBar);
        mButton = findViewById(R.id.buttonHelloStatus);

        findViewById(R.id.buttonHelloActions).setOnClickListener(this::handleHelloActionsButton);
    }

    // method to handle the different types of button clicks
    public void buttonClick(View view) {
        AsyncTask<String, Void, String> task = null;
        String message = ((EditText) findViewById(R.id.inputEditText)).getText().toString();
        switch (view.getId()) {
            case R.id.buttonHelloStatic:
                task = new TestWebServiceTask();
                task.execute(getString(R.string.ep_hello));
                break;
            case R.id.buttonHelloGet:
                task = new GetWebServiceTask();
                task.execute(getString(R.string.ep_base_url),
                        getString(R.string.ep_hello_args),
                        message);
                break;
            case R.id.buttonHelloPost:
                //Build the URL inside of the AsyncTask
                task = new PostWebServiceTask();
                task.execute(getString(R.string.ep_base_url),
                        getString(R.string.ep_hello_args),
                        message);
                break;
            case R.id.buttonHelloStatus:
                // Build the URL outside of the AsyncTask just to see it done differently
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_hello_delay))
                        .build();
                mTask = new StatusWebServiceTask();
                mTask.execute(uri.toString());

                break;
            default:
                throw new IllegalStateException("Not implemented");
        }
    }

    /* onBackPressed method which allows the user to cancel calls by pressing the back button
     */

    @Override
    public void onBackPressed() {
        if(mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
        }
        else {
            super.onBackPressed();
        }
    }
    // inner class for TestWebServiceTask that extends AsyncTask<String, Void, String>

    private class TestWebServiceTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...strings) {
            String response ="";
            HttpURLConnection urlConnection = null;
            String url = strings[0];
            try {
                URL urlObject = new URL(url);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }
            } catch (Exception e) {
                response = "Unable to connect, Reason:"
                        + e.getMessage();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("Unable to")) {
                // Something wrong. This is not the BEST way to handle an error.
                ((EditText) findViewById(R.id.inputEditText)).setError(result);
            } else {
                mTextView.setText(result);
            }
        }


    }

    private class GetWebServiceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...strings) {
            if(strings.length != 3) {
                throw new IllegalArgumentException("Three String arguments required.");
            }
            String response = "";
            HttpURLConnection urlConnection = null;
            // Instead of using a hardcoded (found in end_points.xml) url for our webservice
            // address, here we will build the URL from parts. This can be helpful when
            // sending arguments via GET. In this example, we are sending plain text.
            String url = strings[0];
            String endPoint = strings[1];
            String args = strings[2];
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(url)
                    .appendPath(endPoint)
                    .appendQueryParameter("name", args)
                    .build();
            try {
                URL urlObject = new URL(uri.toString());
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }
            } catch(Exception e) {
                response = "Unable to connect, Reason:"
                        + e.getMessage();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("Unable to")) {
                // Something wrong. This is not the Best way to handle an error.
                ((EditText) findViewById(R.id.inputEditText)).setError(result);
            } else {
                mTextView.setText(result);
            }
        }
    }

    private class PostWebServiceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...strings) {
            if(strings.length != 3) {
                throw new IllegalArgumentException("Three String arguments required.");
            }
            String response = "";
            HttpURLConnection urlConnection = null;
            String url = strings[0];
            String endPoint = strings[1];
            String args = strings[2];
            // build the url
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(url)
                    .appendPath(endPoint)
                    .build();

            // Construct a JSONObject to build a formatted message to send.
            JSONObject msg = new JSONObject();
            try {
                msg.put("name", args);
            } catch (JSONException e) {
                // cancel will result in onCanceled not onPostExecute
                cancel(true);
                return "Error with JSON creation: " + e.getMessage();
            }
            try {
                URL urlObject = new URL(uri.toString());
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(msg.toString());
                wr.flush();
                wr.close();

                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }
            } catch (Exception e) {
                // cancel will result in onCanceled not onPostExecute
                cancel(true);
                return "Unable to connect, Reason: " + e.getMessage();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }
            return response;
        }

        @Override
        protected void onCancelled(String result) {
            super.onCancelled(result);
            ((EditText)findViewById(R.id.inputEditText)).setError(result);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mTextView.setText(result);
        }
    }

    private class StatusWebServiceTask extends AsyncTask<String, Integer, String> {

        private final int NUMBER_OF_OPS = 10;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mProgressBar.setMax(NUMBER_OF_OPS);
            mProgressBar.setProgress(0);
            mButton.setEnabled(false);
        }


        @Override
        protected String doInBackground(String...strings) {
            String response = "";
            HttpURLConnection urlConnection = null;
            String url = strings[0];
            for (int i = 0; i < NUMBER_OF_OPS; i++) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));

                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response = s;
                    }
                    publishProgress(i + 1);
                } catch (Exception e) {
                    response = "Unable to connect, Reason:"
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
                if (isCancelled()) {
                    break;
                }

            }
            return response;
        }

        @Override
        protected void onProgressUpdate(Integer...values) {
            super.onProgressUpdate(values);
            mTextView.setText(Integer.toString(values[0]));
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            finish(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            finish("Canceled");
        }

        private void finish(final String message) {
            mProgressBar.setVisibility(ProgressBar.GONE);
            mTextView.setText(message);
            mButton.setEnabled(true);
            mTask = null;
        }
    }


    /**
     * Handler for the button helloActions.
     * @param helloActionsButton the button itself
     */
    private void handleHelloActionsButton(final View helloActionsButton) {
        String message = ((EditText) findViewById(R.id.inputEditText)).getText().toString();

        // build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_hello_args))
                .build();


        // Build the JSONObject
        JSONObject msg = new JSONObject();
        try {
            msg.put("name", message);
        } catch (JSONException e) {
            Log.e("ACTION", "Error creating JSON: " + e.getMessage());
        }

        // Instantiate and execute the AsyncTask.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleOnPre)
                .onPostExecute(this::handleOnPost)
                .build().execute();
    }

    /**
     * Handler for the AsyncTask's onPreExecute
     */
    private void handleOnPre() {
        findViewById(R.id.buttonHelloActions).setEnabled(false);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
    }

    /**
     * Handler for the AsyncTask's onPostExecute
     */
    private void handleOnPost(final String result) {
        mProgressBar.setVisibility(ProgressBar.GONE);
        mTextView.setText(result);
        findViewById(R.id.buttonHelloActions).setEnabled(true);
    }
}
