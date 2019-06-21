package com.example.jjm_application;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

class SegmentJNI{
    static {
        System.loadLibrary("segmentJNI");
    }
    public native void open();
    public native void print(int num);
    public native void close();
}
class PiezoJNI{
    static {
        System.loadLibrary("piezoJNI");
    }
    public native void open();
    public native void write(int data);
    public native void close();
}
class LedJNI{
    static {
        System.loadLibrary("ledJNI");
    }
    public native void on(int data);
}
class TextlcdJNI{
    static {
        System.loadLibrary("textlcdJNI");
    }
    public native void on();
    public native void off();
    public native void initialize();
    public native void clear();
    public native void print1Line(String msg);
    public native void print2Line(String msg);
}
class DotmatrixJNI{
    static {
        System.loadLibrary("dotmatrixJNI");
    }
    public  native void open();
    public native void DotMatrixControl(String msg);
    public native void close();
}

public class MainActivity extends AppCompatActivity /*implements Runnable*/ {

    SegmentJNI segment;
    PiezoJNI piezo;
    LedJNI led;
    TextlcdJNI textlcd;
    DotmatrixJNI dotmatrix;

    public int a=0;
    public int b=0;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        segment = new SegmentJNI();
        led = new LedJNI();
        piezo = new PiezoJNI();
        textlcd = new TextlcdJNI();
        dotmatrix = new DotmatrixJNI();

        Button button_start = (Button) findViewById(R.id.start_btn);
        Button button_help = (Button) findViewById(R.id.help_btn);

        button_start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"Game Start", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(
                        getApplicationContext(),
                        PlayActivity.class);
                startActivity(intent);
                finish();
            }
        });

        context = this;
        
        button_help.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("게임 설명");

                alertDialogBuilder
                        .setMessage("제한시간 : 10초\n"+
                        "처음 턴 : X\n"+
                                "led : 턴수 표시\n" +
                                "승부가 나지 않을 경우 스코어는 없습니다.\n" +
                                "DotMatrix : 해당 턴인 사람\n" )
                        .setCancelable(false)
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }//onCreate

}
