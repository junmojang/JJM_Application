package com.example.jjm_application;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import static android.view.KeyEvent.*;
import static java.lang.Boolean.*;

public class PlayActivity extends AppCompatActivity{
    /*--------------------declaring  global variation --------------------------------------------*/
    SegmentJNI segment;
    PiezoJNI piezo;
    LedJNI led;
    DotmatrixJNI dotmatrix;
    char winner;
    public Context context =this;
    public boolean flag = FALSE;
    boolean stop = false;
    String buf; // textlcd에 출력할 문장 넣기 위해
    int x_win = 0; // X의 승수
    int o_win = 0; // O의 승
    int lednum =1;
    Thread thread_X,thread_O;

    /*---------------------------keypad가 눌렸는지 확인하는 변수----------------------------------*/
    boolean check[][] = {{false, false, false, false},
                           {false, false, false, false},
                           {false, false, false, false},
                           {false, false, false, false}};
    /*---------------------------승리 알고리즘을 확인하기 위한 변수ㅁ------------------------------*/
    int vic[][] = {{0,0,0,0},
                    {0,0,0,0},
                    {0,0,0,0},
                    {0,0,0,0}};
    TextlcdJNI textlcd = new TextlcdJNI();
    /*--------------------------스레드 종료시 다이얼로그를 띄우기 위해 handler 사용---------------*/
    final Handler mHandler = new Handler(Looper.getMainLooper());
    /*---------------------------사용자 입력을 받아 비기는 조건이 충족하는지 검사-----------------*/
    public boolean tiechecker(){
        int i=0;
        int j=0;
        for(i=0; i<4; i++)
            for(j=0;j<4;j++){
                if(!check[i][j])
                    break;
            }
        if(i==4&&j==4) return true;//tie
        return false;//no tie
    }
    /*---------------------------사용자 입력을 받아 승리조건이 충족하는지 검사--------------------*/
    public int winner(int x, int y, int checker) {
        int i=0;
        if(x==y)//0,0 to 3,3
        {
            for(i=0; i<4; i++){
                if(vic[i][i]!=checker) break;
            }
            if(i==4) return 1;//승리조건 만족
        }
        else if(y==(3-x))//0,3 to 3,0
        {
            for(i=0; i<4; i++){
                if(vic[i][3-i]!=checker) break;
            }
            if(i==4) return 1;//승리조건 만족
        }

        for(i=0;i<4;i++) {
            if(vic[x][i]!=checker)break;
        }
        if(i==4) return 1;//승리조건 만족

        for(i=0;i<4;i++)
        {
            if(vic[i][y]!=checker) break;
        }
        if(i==4) return 1;//승리조건 만족

        return 0;//승리조건 불만족
    }
    /*--------------------------누가 이겼는지 flag를 통해 winner 설정-----------------------------*/
    char who_win(){
        if(flag==true){
            winner = 'X';
        }
        else {
            winner = 'O';
        }
        return winner;
    }
    /*--------------------------ketpad 작동을 위한 함수-------------------------------------------*/
    public boolean onKeyUp(int keyCode, KeyEvent event){
        switch (keyCode){

            case  KEYCODE_0 :
                if(check[0][0] != true) {
                    check[0][0] = true;
                    ImageButton btn_11 = findViewById(R.id.btn_11);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true; // 전 스레드의 while 문 끝내기 용

                    if (flag == TRUE) {
                        lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_11.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        vic[0][0]=1;
                        int win=winner(0,0,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        stop = false; // 다음 스레드 시작하기 위해
                        btn_11.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        vic[0][0]=-1;
                        int win=winner(0,0,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }

                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                } catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_1 :
                if(check[0][1] != true) {
                    check[0][1] = true;
                    ImageButton btn_12 = findViewById(R.id.btn_12);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[0][1]=1;
                        int win=winner(0,1,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_12.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[0][1]=-1;
                        int win=winner(0,1,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }

                        stop = false;
                        btn_12.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_2 :
                if(check[0][2] != true) {
                    check[0][2] = true;
                    ImageButton btn_13 = findViewById(R.id.btn_13);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[0][2]=1;
                        int win=winner(0,2,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_13.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[0][2]=-1;
                        int win=winner(0,2,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_13.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_3 :
                if(check[0][3] != true) {
                    check[0][3] = true;
                    ImageButton btn_14 = findViewById(R.id.btn_14);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[0][3]=1;
                        int win=winner(0,3,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_14.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[0][3]=-1;
                        int win =winner(0,3,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_14.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_4 :
                if(check[1][0] != true) {
                    check[1][0] = true;
                    ImageButton btn_21 = findViewById(R.id.btn_21);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[1][0]=1;
                        int win=winner(1,0,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_21.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[1][0]=-1;
                        int win=winner(1,0,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_21.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_5 :
                if(check[1][1] != true) {
                    check[1][1] = true;
                    ImageButton btn_22 = findViewById(R.id.btn_22);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[1][1]=1;
                        int win=winner(1,1,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        led.on(lednum);
                        stop = false;
                        btn_22.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[1][1]=-1;
                        int win=winner(1,1,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_22.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_6 :
                if(check[1][2] != true) {
                    check[1][2] = true;
                    ImageButton btn_23 = findViewById(R.id.btn_23);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[1][2]=1;
                        int win=winner(1,2,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_23.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[1][2]=-1;
                        int win=winner(1,2,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_23.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_7 :
                if(check[1][3] != true) {
                    check[1][3] = true;
                    ImageButton btn_24 = findViewById(R.id.btn_24);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[1][3]=1;
                        int win=winner(1,3,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_24.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[1][3]=-1;
                        int win=winner(1,3,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_24.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_8 :
                if(check[2][0] != true) {
                    check[2][0] = true;
                    ImageButton btn_31 = findViewById(R.id.btn_31);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[2][0]=1;
                        int win=winner(2,0,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_31.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[2][0]=-1;
                        int win=winner(2,0,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_31.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_9 :
                if(check[2][1] != true) {
                    check[2][1] = true;
                    ImageButton btn_32 = findViewById(R.id.btn_32);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[2][1]=1;
                        int win=winner(2,1,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_32.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[2][1]=-1;
                        int win=winner(2,1,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_32.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_Q :
                if(check[2][2] != true) {
                    check[2][2] = true;
                    ImageButton btn_33 = findViewById(R.id.btn_33);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[2][2]=1;
                        int win=winner(2,2,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_33.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[2][2]=-1;
                        int win=winner(2,2,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_33.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_W :
                if(check[2][3] != true) {
                    check[2][3] = true;
                    ImageButton btn_34 = findViewById(R.id.btn_34);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[2][3]=1;
                        int win=winner(2,3,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_34.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[2][3]=-1;
                        int win=winner(2,3,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_34.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_E :
                if(check[3][0] != true) {
                    check[3][0] = true;
                    ImageButton btn_41 = findViewById(R.id.btn_41);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[3][0]=1;
                        int win=winner(3,0,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_41.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[3][0]=-1;
                        int win=winner(3,0,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();

                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_41.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_R :
                if(check[3][1] != true) {
                    check[3][1] = true;
                    ImageButton btn_42 = findViewById(R.id.btn_42);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[3][1]=1;
                        int win=winner(3,1,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_42.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[3][1]=-1;
                        int win=winner(3,1,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_42.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_T :
                if(check[3][2] != true) {
                    check[3][2] = true;
                    ImageButton btn_43 = findViewById(R.id.btn_43);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[3][2]=1;
                        int win=winner(3,2,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_43.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[3][2]=-1;
                        int win=winner(3,2,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_43.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;
            case  KEYCODE_Y :
                if(check[3][3] != true) {
                    check[3][3] = true;
                    ImageButton btn_44 = findViewById(R.id.btn_44);
                    piezo.write(0x21);
                    for (int buf = 0; buf < 5000; buf++) ;
                    piezo.write(0x0);
                    stop = true;

                    if (flag == TRUE) {
                        vic[3][3]=1;
                        int win=winner(3,3,1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    o_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                         if(tiechecker()){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_X.interrupt();
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage(
                                                    "무승부!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
lednum = (lednum * 2) + 1;
                        led.on(lednum);
                        stop = false;
                        btn_44.setBackgroundResource(R.drawable.o);
                        flag = FALSE;
                        thread_O.interrupt();
                        thread_X = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00006314080814630000"); //X
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                o_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_X.start();
                    } else {
                        vic[3][3]=-1;
                        int win=winner(3,3,-1);
                        if(win==1){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    thread_O.interrupt();
                                    x_win++;
                                    buf = "      "+o_win + " : " + x_win;
                                    textlcd.print2Line(buf);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setTitle("Result");
                                    alertDialogBuilder
                                            .setMessage( who_win() +
                                                    " 승리!\n")
                                            .setCancelable(false)
                                            .setNegativeButton("홈으로",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(
                                                                DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(
                                                                    getApplicationContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                    final AlertDialog alertDialog = alertDialogBuilder.create();

                                    alertDialog.show();
                                }
                            }, 0);
                        }
                        stop = false;
                        btn_44.setBackgroundResource(R.drawable.x);
                        flag = TRUE;
                        thread_X.interrupt();
                        thread_O = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (!stop) {
                                        for (int i = 1000; i >= 0; i--) {
                                            segment.print(i);
                                            dotmatrix.DotMatrixControl("00001c224141221c0000"); //O
                                            Thread.sleep(0);
                                        }
                                        stop = true;
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                x_win++;
                                                buf = "      "+o_win + " : " + x_win;
                                                textlcd.print2Line(buf);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle("Time Out!");
                                                alertDialogBuilder
                                                        .setMessage("시간초과\n" + who_win() +
                                                                " 승리!\n")
                                                        .setCancelable(false)
                                                        .setNegativeButton("홈으로",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(
                                                                            DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        Intent intent = new Intent(
                                                                                getApplicationContext(),
                                                                                MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                final AlertDialog alertDialog = alertDialogBuilder.create();

                                                alertDialog.show();
                                            }
                                        }, 0); // mHandler
                                    }//while
                                }//try
                                catch (InterruptedException e) {
                                }
                            }//run
                        }//Runnable
                        );//Thread
                        thread_O.start();
                    }
                }
                break;


        }
        return false;
    } // keypad 작동

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_screen);

        /*--------------------승리 수를 저장하기 위해 preference key 이용-------------------------*/
        SharedPreferences pref = getSharedPreferences("pref", 0);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt("x_win", x_win);
        editor.putInt("o_win", o_win);
        editor.commit();

        String buf0 = "score O : X";
        String buf1 = "      "+o_win + " : " + x_win;

        textlcd.on();
        textlcd.initialize();
        textlcd.clear();
        textlcd.print1Line(buf0);
        textlcd.print2Line(buf1);

        final Handler mHandler = new Handler(Looper.getMainLooper());

        /*---------------------첫 시작인 x의 thread 설정------------------------------------------*/
        thread_X = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!stop) {
                        for (int i = 1000; i >= 0; i--) {
                            segment.print(i);//timer 역할
                            dotmatrix.DotMatrixControl("00006314080814630000"); //X의 모양
                            Thread.sleep(0);//스레드 종료시 0밀리초 대기
                        }
                        stop = true;// while문을 빠져나오기 위한 트리거
                        mHandler.postDelayed(new Runnable() {//스레드 내에 스레드 사용을 위한 handler 설정
                            @Override
                            public void run() {
                                o_win++; //o의 승수 +1
                                editor.putInt("o_win", o_win);
                                editor.commit(); // preference key에 값을 저장장
                                buf = "      "+o_win + " : " + x_win;
                                textlcd.print2Line(buf);
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context); //timer dialog build
                                alertDialogBuilder.setTitle("Time Out!"); // dialog 제목
                                alertDialogBuilder
                                        .setMessage("시간초과\n" +
                                                "O 승리!\n")
                                        .setCancelable(false)
                                        .setNegativeButton("홈으로", // 취소 버튼 클릭시
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(
                                                            DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        Intent intent = new Intent(
                                                                getApplicationContext(),
                                                                MainActivity.class);
                                                        startActivity(intent); //취소 버튼 클릭시 메인 화면으로
                                                        finish(); // 현제 페이지를 layer에서 삭제
                                                    }
                                                });
                                final AlertDialog alertDialog = alertDialogBuilder.create(); // dialog 생성
                                alertDialog.show(); // dialog 띄우기
                            }
                        }, 0); // mHandler
                    }//while
                }//try
                catch (InterruptedException e) {
                }
            }//run
        }//Runnable
        );//Thread

        segment = new SegmentJNI();
        dotmatrix = new DotmatrixJNI();
        piezo = new PiezoJNI();
        led = new LedJNI();

        segment.open();
        dotmatrix.open();
        piezo.open();
        led.on(lednum);

        thread_X.start();//x의 스레드 시작

        /*-------------------------뒤로 가기 버튼 설정--------------------------------------------*/
        final ImageButton btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                stop=true;
                if(flag==true)thread_O.interrupt();
                else{thread_X.interrupt();}
                segment.close();
                piezo.close();
                textlcd.clear();
                led.on(00000000);
                dotmatrix.close();

                Intent intent = new Intent(
                        getApplicationContext(),
                        MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}//main
