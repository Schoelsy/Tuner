package me.schoe.tunererer;

import android.app.Activity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
//import flanagan.*;
import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import flanagan.math.FourierTransform;

import static java.lang.Math.sqrt;

public class MainActivity extends Activity {


    protected TextView _percentField;
    protected InitTask _initTask;

    //Properties (MIC)
    public AudioRecord audioRecord;
    public int mSamplesRead; //how many samples read
    public int recordingState;
    public int buffersizebytes;
    public int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static short[] buffer; //+-32767
    public static final int SAMPPERSEC = 44100; //samp per sec 8000, 11025, 22050 44100 or 48000

    public List<Double> fr = new ArrayList<Double>();

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );

        _percentField = ( TextView ) findViewById( R.id.freqTextID );

        buffersizebytes = AudioRecord.getMinBufferSize(SAMPPERSEC,channelConfiguration,audioEncoding); //4096 on ion
        buffer = new short[buffersizebytes];
        audioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC,SAMPPERSEC,channelConfiguration,audioEncoding,buffersizebytes); //constructor

        _initTask = new InitTask();
        _initTask.execute( this );
    }

    /**
     * sub-class of AsyncTask
     */
    protected class InitTask extends AsyncTask<Context, Double, String>
    {
        @Override
        protected String doInBackground( Context... params )
        {

            audioRecord.startRecording();

            while( true )
            {
                try{
                    //Log.d("Potato", String.valueOf(1));

                    //n
                    mSamplesRead = audioRecord.read(buffer, 0, buffersizebytes);

                   // Log.d("Potato", String.valueOf(2));
                    int otat = 0;

                    otat++;

                    //Log.d("Potato", String.valueOf(3));
                    double dables[] = new double[mSamplesRead];
                    for (int f = 0; f < mSamplesRead; f++){
                        dables[f] = (double)buffer[f];
                    }

                    //Log.d("Potato", String.valueOf(5));
                    double magn[] = new double[mSamplesRead];

                    DoubleFFT_1D fft = new DoubleFFT_1D(dables.length-1);
                    fft.realForward(dables);

                    //Log.d("Potato", String.valueOf(6));

                    for (int i = 0 ; i < (mSamplesRead / 2) - 1 ; i++) {
                        double re = dables[2 * i];
                        double im = dables[2 * i + 1];
                        magn[i] = sqrt(re * re + im * im);
                    }

                    double max_magnitude = -99999999;
                    int max_index = -1;
                    for (int i = 0 ; i < (mSamplesRead / 2) - 1; i++){
                        if (magn[i] > max_magnitude){
                            max_magnitude = magn[i];
                            max_index = i;
                        }
                    }

// convert index of largest peak to frequency
                    double freq = max_index * SAMPPERSEC / mSamplesRead;
                    //Log.d("Potato", String.valueOf(7));

                    if(freq > 20 && freq < 300){
                        fr.add(freq);

                        if(fr.size() == 5){
                            double med=0;
                            for (int i = 0; i < 5; i++){
                                med = med + fr.get(i);
                            }
                            med = med / 5;
                            fr.clear();
                            publishProgress(med);
                           // Log.d("Potato", String.valueOf(med));
                        }

                        //Log.d("Potato", String.valueOf(freq));

                    }




                    //Log.d("Potato", String.valueOf(8));
                } catch( Exception e ){
                }
            }
        }


        @Override
        protected void onProgressUpdate(Double... calc){
            super.onProgressUpdate(calc);
            _percentField.setText( String.valueOf(calc[0]) );
        }

       /* @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            //Log.i( "makemachine", "onProgressUpdate(): " +  String.valueOf( values[0] ) );
            //_percentField.setText( String.valueOf(values[0]) );
        } */

        @Override
        protected void onPostExecute( String result )
        {
            super.onPostExecute(result);
            //Log.i( "makemachine", "onPostExecute(): " + result );
        }


    }
}

