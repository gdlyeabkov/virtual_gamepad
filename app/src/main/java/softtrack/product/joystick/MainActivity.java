package softtrack.product.joystick;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    InputStream remoteBluetoothInputStream = null;
    OutputStream remoteBluetoothOutputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // findUsbDevice();
        findBluetoothDevice();

        getRemoveBluetoothData();

        initializeGamePadControl();

    }

    public void findBluetoothDevice() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        boolean isBluetoothAdapterExists = bluetoothAdapter != null;
        MediaPlayer m = MediaPlayer.create(getApplicationContext(), R.raw.not_connected_devices);
        if (isBluetoothAdapterExists) {
            boolean isBluetoothAdapterEnabled = bluetoothAdapter.isEnabled();
            if (isBluetoothAdapterEnabled) {
                Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
                boolean isDevicesNotConnected = bluetoothDevices.isEmpty();
                boolean isDevicesConnected = !isDevicesNotConnected;
                if (isDevicesConnected) {
                    Object[] devices = bluetoothDevices.toArray();
                    BluetoothDevice neededBluetoothDevice = null;
                    for (Object device : devices) {
                        BluetoothDevice bluetoothDevice = (BluetoothDevice) device;
                        String bluetoothDeviceName = bluetoothDevice.getName();
                        Log.d("debug", "Устройство с именем: " + bluetoothDeviceName);
                        String playingPCName = "LAPTOP-CTRU4FC7";
                        boolean isPlayingPCName = bluetoothDeviceName.contains(playingPCName);
                        if (isPlayingPCName) {
                            neededBluetoothDevice = bluetoothDevice;
                            break;
                        }
                    }
                    boolean isBluetoothDeviceExists = neededBluetoothDevice != null;
                    if (isBluetoothDeviceExists) {
                        String connectedDeviceName = neededBluetoothDevice.getName();
                        Log.d("debug", "Игровое устройство: " + connectedDeviceName);
                        m = MediaPlayer.create(getApplicationContext(), R.raw.have_connected_devices);

                        ParcelUuid[] uuids = neededBluetoothDevice.getUuids();
                        boolean isHaveUUIDs = uuids.length >= 1;
                        if (isHaveUUIDs) {
                            ParcelUuid rawUUID = uuids[0];
                            UUID uuid = rawUUID.getUuid();
                            BluetoothSocket socket = null;
                            try {
                                socket = neededBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                                socket.connect();
                                remoteBluetoothOutputStream = socket.getOutputStream();
                                remoteBluetoothInputStream = socket.getInputStream();
                                // socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    Log.d("debug", "нет подключенных bluetooth-устройств");
                }
            }
        }
        m.start();

    }

    public void findUsbDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();

        Iterator<UsbDevice> it = devices.values().iterator();

        boolean isDevicesNotConnected = devices.isEmpty();
        boolean isDevicesConnected = !isDevicesNotConnected;

        isDevicesConnected = !devices.values().isEmpty();

        MediaPlayer m = null;
        if (isDevicesConnected) {
//            UsbDevice possibleDevice = devices.get("<device-name>");
            Set<String> rawDevicesNames = devices.keySet();
            Object[] devicesNames = rawDevicesNames.toArray();
            Object rawFirstDeviceName = devicesNames[0];
            String firstDeviceName = rawFirstDeviceName.toString();
            UsbDevice possibleDevice = devices.get(firstDeviceName);
            String possibleDeviceName = possibleDevice.getDeviceName();
            Log.d("debug", "Имя подключенного устройства " + possibleDeviceName);
            m = MediaPlayer.create(getApplicationContext(), R.raw.have_connected_devices);
            while(it.hasNext()) {
                UsbDevice device = it.next();
                // use device info

            }
        }
        else {
            Log.d("debug", "нет подключенных устройств");
            m = MediaPlayer.create(getApplicationContext(), R.raw.not_connected_devices);
        }

        m.start();
    }

    void getRemoveBluetoothData() {
        Timer bluetoothRemoteDataDetector = new Timer();
        bluetoothRemoteDataDetector.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                boolean isRemoteBluetoothStreamExists = remoteBluetoothInputStream != null;
                if (isRemoteBluetoothStreamExists) {
                    final int BUFFER_SIZE = 1024;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytes = 0;
                    try {
//                        remoteBluetoothInputStream.read(buffer, bytes, BUFFER_SIZE - bytes);
                        bytes = remoteBluetoothInputStream.read(buffer);
                        String rawInputBytes = String.valueOf(bytes);
                        Log.d("debug", rawInputBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("debug", "Не могу прочесть нет входного потока");
                }
            }
        }, 0, 2000);
    }

    public void initializeGamePadControl() {
        Button aActionBtn = findViewById(R.id.aActionBtn);
        Button bActionBtn = findViewById(R.id.bActionBtn);
        Button cActionBtn = findViewById(R.id.cActionBtn);
        Button dActionBtn = findViewById(R.id.dActionBtn);
        Button upMovementBtn = findViewById(R.id.upMovementBtn);
        Button downMovementBtn = findViewById(R.id.downMovementBtn);
        Button leftMovementBtn = findViewById(R.id.leftMovementBtn);
        Button rightMovementBtn = findViewById(R.id.rightMovementBtn);
        aActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // посылаем прерывание действия 1
            }
        });
        bActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // посылаем прерывание действия 2
            }
        });
        cActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // посылаем прерывание действия 3
            }
        });
        dActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // посылаем прерывание действия 4
            }
        });
        upMovementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // посылаем прерывание движения вверх
            }
        });
        downMovementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // посылаем прерывание движения вниз
            }
        });
        leftMovementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // посылаем прерывание движения влево
            }
        });
        rightMovementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // посылаем прерывание движения вправо
            }
        });
    }

}