package trungt.tcss450.uw.edu.webservicesandbox;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                break;
            case R.id.buttonHelloPost:
                break;
            case R.id.buttonHelloStatus:
                break;
            default:
                throw new IllegalStateException("Not implemented");
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


}
