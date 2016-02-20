package org.main.smartmirror.smartmirror;


import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class NsdHelper {

    Context mContext;

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;
    private boolean serviceRegistered = false;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "NsdHelper";

    // unique device name
    public String mDeviceName = APP_NAME;
    // name given to this instance by the service
    public String mServiceName;
    public static final String APP_NAME = "SmartMirror";

    NsdServiceInfo mService;

    public NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        // The device name is of the form "SmartMirror_ID" where ID is the device's system ID
        // This is used to distinguish this machine from other broadcasters on the network.
        mDeviceName += "_" + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        //initializeRegistrationListener();
    }

    public void initializeNsd() {
        //initializeRegistrationListener();
    }

    public void initializeDiscoveryListener() {
        mResolveListener = new MyResolveListener();
        mDiscoveryListener = new MyDiscoveryListener();
        discoverServices();
    }

    public class MyDiscoveryListener implements NsdManager.DiscoveryListener {

        @Override
        public void onDiscoveryStarted(String regType) {
            Log.d(TAG, "onDiscoveryStarted()");
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            Log.d(TAG, "service found :: " + service);
            if (!service.getServiceType().equals(SERVICE_TYPE)) {
                Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
            } else if (service.getServiceName().equals(mServiceName)) {
                Log.d(TAG, "Same machine: " + mServiceName);
            } else if (service.getServiceName().contains(APP_NAME)) {
                mNsdManager.resolveService(service, mResolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            Log.e(TAG, "service lost :: " + service);
            if (mService == service) {
                mService = null;
            }
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.i(TAG, "Discovery stopped:" + serviceType);
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }
    }


    private class MyResolveListener implements NsdManager.ResolveListener {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "Resolve failed" + errorCode);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Resolve Succeeded :: " + serviceInfo.getHost().toString());

            if (serviceInfo.getServiceName().equals(mServiceName)) {
                Log.d(TAG, "Same IP :: " + serviceInfo.getHost().toString());
                return;
            }
            mService = serviceInfo;
            //((MainActivity)mContext).connectToRemote(mService);
        }

    }
    /*
    public void initializeRegistrationListener() {
        Log.i(TAG, "initializeRegistrationListener()");
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                mServiceName = nsdServiceInfo.getServiceName();
                Log.d(TAG, "service registered as :: " + nsdServiceInfo);
                serviceRegistered = true;
                initializeDiscoveryListener();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.e(TAG, "Registration Failed :: " + arg1);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                serviceRegistered = false;
                Log.d(TAG, "service unregistered");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };
    }
    */

    public class MyRegistrationListener implements NsdManager.RegistrationListener {
        @Override
        public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
            mServiceName = nsdServiceInfo.getServiceName();
            Log.d(TAG, "service registered as :: " + nsdServiceInfo);
            serviceRegistered = true;
            initializeDiscoveryListener();
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            serviceRegistered = false;
            Log.e(TAG, "Registration Failed :: " + arg1);
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
            serviceRegistered = false;
            Log.d(TAG, "ServiceUnregistered :: " + arg0);
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        }
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mDeviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        Log.d(TAG, "serviceInfo :: " + serviceInfo);
        unregisterService();
        mRegistrationListener = new MyRegistrationListener();
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

    }

    public void discoverServices() {
        if (serviceRegistered) {
            Log.i(TAG, "NsdHelper.discoverServices :: " + mDiscoveryListener);
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        } else {
            Log.e(TAG, "NsdHelper.discoverServices SERVICE NOT REGISTERED");
        }
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    public void unregisterService(){
        if (mRegistrationListener != null) {
            mNsdManager.unregisterService(mRegistrationListener);
        }
    }

    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
        mResolveListener = null;
    }
}
