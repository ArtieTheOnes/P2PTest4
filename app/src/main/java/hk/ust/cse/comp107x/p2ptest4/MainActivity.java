package hk.ust.cse.comp107x.p2ptest4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    private final IntentFilter mIntentFilter = new IntentFilter();



    static final int SocketServerPORT = 8080;                                      //set PORT number
    ServerSocket serverSocket;   //=================================================================
    ServerSocketThread serverSocketThread;


    //static final int SocketClientPORT = 1996;


    private Button connectButton;
    private Button searchButton;
    private Button sendButton;
    private ToggleButton enableToggleButton;
    //private Button disConnectButton;

    /*
    public TextView startConnectTime;
    public TextView finishConnectTime;
    public TextView needConnectTime;
    */

    public TextView stateText;
    private TextView connectTimeText;
    public EditText ipEditText;


    List peersshow = new ArrayList();

    ArrayList<String> peersname = new ArrayList<String>(){};

    ArrayAdapter<String> madapter;

    public static String startTime;
    public static String endTime;

    //public static boolean enablenum = false;
    public static int peerpick = 0;

    String myDeviceName;
    public static boolean isOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);


        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();  //=============================================================


        connectButton = (Button) findViewById(R.id.connectButton);
        stateText = (TextView) findViewById(R.id.stateText);
        searchButton = (Button) findViewById(R.id.searchButton);
        enableToggleButton = (ToggleButton) findViewById(R.id.enableToggleButton);
        connectTimeText = (TextView) findViewById(R.id.connectTimeText);
        sendButton = (Button) findViewById(R.id.sendButton);
        ipEditText = (EditText) findViewById(R.id.ipEditText);

        /*
        disConnectButton = (Button) findViewById(R.id.disConnectButton);
        startConnectTime = (TextView) findViewById(R.id.start_connect_time);
        finishConnectTime = (TextView) findViewById(R.id.finish_connect_time);
        needConnectTime = (TextView) findViewById(R.id.need_connect_time);
        */


        searchButton.setOnClickListener(searchButtonClick);
        connectButton.setOnClickListener(connectButtonClick);
        enableToggleButton.setOnClickListener(enableToggleButtonClick);
        sendButton.setOnClickListener(sendButtonClick);
        //disConnectButton.setOnClickListener(disConnectButtonClick);


        madapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peersname);
        ListView peersListView = (ListView) findViewById(R.id.peersListView);
        peersListView.setAdapter(madapter);


        peersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                peerpick = position;
                stateText.setText(peersname.get(peerpick));

            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();                                                   //MUST close
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//==================================================================================================//

    View.OnClickListener searchButtonClick = new View.OnClickListener() {   //----------SearchButton

        public void onClick(View v){
            //startConnectTime.setText(getTime());

            getTime(0);
            Log.d("Toast", "Searching~");
            stateText.setText("Search~");


            stateText.setText(getIpAddress());
            Log.d("Toast", getIpAddress());

            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.d("Toast", Integer.toString(reasonCode));

                }
            });
        }
    };


    public void connect(){  //---------------------------------------------------------------CONNECT


        stateText.setText("Starting connection with: " + peersname.get(peerpick));

        WifiP2pDevice device = (WifiP2pDevice) peersshow.get(peerpick);   //modified
        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = device.deviceAddress;
        final String deviceName = device.deviceName;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                Log.d("Toast", "Connection Init Successful!~");

                stateText.setText("Connected with: " + deviceName);
                //finishConnectTime.setText(getTime());
                getTime(1);

                displayTime();
            }

            @Override
            public void onFailure(int reason) {
                Log.d("Toast", "Connect failed. Retry.");
            }
        });
    }


    View.OnClickListener sendButtonClick = new View.OnClickListener() {   //----------SendButton

        public void onClick(View v){    //----------------------------------------SEND
            getTime(0);
            Log.d("Toast", "Sending~");
            stateText.setText("Send~");

            String foo = ipEditText.getText().toString();
            if(foo == ""){
                foo = "193";
            }

            FileTxThread fileTxThread = new FileTxThread("192.168.49." + foo, SocketServerPORT);  //=====
            fileTxThread.start();
        }
    };


    //@Override
    public void disconnect() {  //--------------------------------------------------------DISCONNECT

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d("Toast", "Disconnect failed. Reason :" + reasonCode);
                //finishConnectTime.setText(getTime());

                //displayTime();
            }

            @Override
            public void onSuccess() {
                Log.d("Toast", "Disconnect Success");
                stateText.setText("Hello world!");
                //finishConnectTime.setText(getTime());

                //displayTime();
            }

        });
    }


    public View.OnClickListener connectButtonClick = new View.OnClickListener() {  //-----connectButton

        public void onClick(View v){
            //startConnectTime.setText(getTime());
            getTime(0);
            Log.d("Toast", "Connecting~");
            //Toast.makeText(getBaseContext(), "Connecting~", Toast.LENGTH_SHORT).show();
            stateText.setText("Connection Init");

            try {

                connect();

            } catch (Exception ex) {
                Log.d("Toast", "Connection Failed, PLZ try again");

            }

        }
    };



    View.OnClickListener enableToggleButtonClick = new View.OnClickListener() {   //----enableToggleButton

        public void onClick(View v){
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            if (enableToggleButton.isChecked()) {
                wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi
            }
            else {
                wifi.setWifiEnabled(false); // true or false to activate/deactivate wifi
            }
        }
    };



    public void displayTime(){
        float foo;
        //foo = Float.parseFloat(finishConnectTime.getText().toString()) - Float.parseFloat(startConnectTime.getText().toString());
        foo = Float.parseFloat(endTime) - Float.parseFloat(startTime);

        if (foo < 0){
            foo += 60;
        }

        connectTimeText.setText(Float.toString(foo));
    }

    public void getTime(int foo){

        Calendar now = Calendar.getInstance();
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        String timer = (Integer.toString(second) + "." + Integer.toString(millis));

        Log.d("Toast", "Get Time: " + timer);

        if(foo == 0){
            startTime = timer;
        }
        else if(foo == 1){
            endTime = timer;
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);


                while (true) {
                    socket = serverSocket.accept();
                    FileRxThread fileRxThread = new FileRxThread(socket);
                    fileRxThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }



    private class FileRxThread extends Thread {

        Socket socket;
        FileRxThread(Socket socket){ this.socket= socket; }

        @Override
        public void run() {
            //Socket socket = null;

            try {

                //String filename = filenameEditText2.getText().toString();
                String filename = "instruction.txt";

                File file;


                do{
                    file = new File(Environment.getExternalStorageDirectory(), filename);          //get the file!!!
                    filename = "1" + filename;
                } while(file.exists());


                byte[] bytes = new byte[1024];

                InputStream is = socket.getInputStream();

                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                int bytesRead = is.read(bytes, 0, bytes.length);
                bos.write(bytes, 0, bytesRead);

                bos.close();
                socket.close();

                Log.d("Toast", "File Received!");

                try {
                    reflectMethod("instruction.txt");
                    Log.d("reflect", "success");
                } catch (Exception e) {
                    Log.d("reflect",e.toString());
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if(socket != null){
                    try {
                        socket.close();                                     //MUST close all sockets
                        //getTime(1);
                        //displayTime();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }



    public class FileTxThread extends Thread {
        String dstAddress;
        int dstPort;

        FileTxThread(String address, int port) {                                  //initialization
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {

            Socket socket = null;

            try{
                socket = new Socket(dstAddress, dstPort);

                //String filename = filenameEditText.getText().toString();
                String filename = "instruction.txt";
                File file = new File( Environment.getExternalStorageDirectory(), filename);             //give the file-to-send's name


                byte[] bytes = new byte[(int) file.length()];
                BufferedInputStream bis;


                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytes, 0, bytes.length);

                OutputStream os = socket.getOutputStream();
                os.write(bytes, 0, bytes.length);
                os.flush();

                //socket.close();

                Log.d("Toast", "File sent to: " + socket.getInetAddress());



            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    //getTime(1);
                    //displayTime();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    public void reflectMethod(String fileName) throws Exception{
        String transmitter = "", className = "", methodName = "", parameter, dialog="";
        Vector<String> parameterList = new Vector<String>();
        int parameterListSize = 0;
        File inputFile = new File( Environment.getExternalStorageDirectory(), fileName);
        if (inputFile.exists());
        else {Log.d("reflect inside", "can not found file"); return;}
        Scanner input = new Scanner(inputFile);
        String identifier = "";
        if (input.hasNext()) identifier = input.next();
        if (!identifier.equals("p2pfile"))
        {
            //error
            return;
        }
        while (input.hasNext())
        {
            identifier = input.next();
            switch (identifier)
            {
                case "transmitter": transmitter = input.next(); break;
                case "className"  : className = input.next(); break;
                case "methodName" : methodName = input.next(); break;
                case "parameterListSize": parameterListSize = input.nextInt(); break;
                case "parameter" :
                    parameter = input.nextLine();
                    for (int i = 1; i <= parameterListSize; ++i)
                    {
                        parameter = input.nextLine();
                        parameterList.add(parameter);
                    } break;
                default : Log.d("Unidentified identifier",identifier);
            }
        }
        input.close();
        dialog += "Instruction received from device " + transmitter +'\n';
        dialog += "Reflecting class " + className +'\n';
        dialog += "Invoking method: " + methodName +'\n';
        dialog += "Invoking with " + parameterListSize + " parameters" +'\n';
        dialog += "Parameter List: \n";
        for (int i = 1; i <= parameterListSize; ++i)
            dialog += parameterList.elementAt(i-1) +'\n';
        Log.d("dialog", dialog);

    }

}

