package com.legendmohe.permissionutil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by legendmohe on 2017/1/3.
 */

public class PermissionUtil {
    private static final String TAG = "PermissionUtil";

    private static List<PermissionItem> sPendingRequest = new ArrayList<>();

    private PermissionUtil() {
    }

    //////////////////////////////////////////////////////////////////////

    public static <T> PermissionItem with(T context) {
        if (context == null) {
            throw new NullPointerException("context is null");
        }
        if (!(context instanceof Activity) && !(context instanceof Fragment)) {
            throw new IllegalArgumentException("context must be Activity or Fragment");
        }

        PermissionItem<T> item = findPermissionItemOf(context);
        if (item != null) {
            return item;
        } else {
            return pushRequest(context);
        }
    }

    private static <T> PermissionItem<T> pushRequest(T context) {
        PermissionItem<T> newItem = new PermissionItem<>();
        newItem.mPermissionContext = new WeakReference<>(context);
        sPendingRequest.add(newItem);
        return newItem;
    }

    private static <T> PermissionItem<T> findPermissionItemOf(T context) {
        for (PermissionItem item :
                sPendingRequest) {
            if (item.mPermissionContext.get() != null && item.mPermissionContext.get() == context) {
                return item;
            }
        }
        return null;
    }

    public static void onRequestPermissionsResult(Activity context, int requestCode, String[] permissions,
                                                  int[] grantResults) {
        PermissionItem<?> item = findPermissionItemOf(context);
        if (item == null) {
            return;
        }
        if (requestCode != item.mRequestCode) {
            return;
        }
        sPendingRequest.remove(item);

        Set<String> denied = new HashSet<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                denied.add(permissions[i]);
            }
        }
        if (item.mCallback != null) {
            if (denied.size() != 0) {
                item.mCallback.onPermissionDenied(denied.toArray(new String[denied.size()]));
            } else {
                item.mCallback.onPermissionGranted(item.mPermissions.toArray(new String[item.mPermissions.size()]));
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

    public static final class PermissionItem<T> {
        private WeakReference<T> mPermissionContext;
        private int mRequestCode;
        private Callback mCallback;
        private Set<String> mPermissions = new HashSet<>();

        public PermissionItem() {
            mRequestCode = new Random().nextInt(100) + 100;
        }

        public PermissionItem<T> addPermission(String permission) {
            mPermissions.add(permission);
            return this;
        }

        public PermissionItem<T> addPermissions(String[] permissions) {
            mPermissions.addAll(Arrays.asList(permissions));
            return this;
        }

        public PermissionItem<T> addPermissions(Collection<String> permissions) {
            mPermissions.addAll(permissions);
            return this;
        }

        public PermissionItem<T> setCallback(Callback callback) {
            mCallback = callback;
            return this;
        }

        public PermissionItem<T> setRequestCode(int requestCode) {
            mRequestCode = requestCode;
            return this;
        }

        public void request() {
            if (mPermissions.size() == 0) {
                if (mCallback != null) {
                    mCallback.onPermissionGranted(new String[0]);
                }
            } else {
                if (mPermissionContext.get() == null) {
                    return;
                }

                Set<String> denied = new HashSet<>();
                for (String permission :
                        mPermissions) {
                    if (checkContextPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        denied.add(permission);
                    }
                }
                if (denied.size() == 0) {
                    if (mCallback != null) {
                        mCallback.onPermissionGranted(mPermissions.toArray(new String[mPermissions.size()]));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestContextPermissions(denied.toArray(new String[denied.size()]), mRequestCode);
                    } else {
                        if (mCallback != null) {
                            mCallback.onPermissionDenied(denied.toArray(new String[denied.size()]));
                        }
                    }
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        private void requestContextPermissions(String[] permissions, int requestCode) {
            T context = mPermissionContext.get();
            if (context instanceof Activity) {
                ((Activity) context).requestPermissions(permissions, requestCode);
            } else if (context instanceof Fragment) {
                ((Fragment) context).requestPermissions(permissions, requestCode);
            }
        }

        private int checkContextPermission(String permission) {
            T context = mPermissionContext.get();
            if (context instanceof Activity) {
                return ((Activity) context).checkCallingOrSelfPermission(permission);
            } else if (context instanceof Fragment) {
                return ((Fragment) context).getActivity().checkCallingOrSelfPermission(permission);
            }
            return -1;
        }
    }

    public interface Callback {
        void onPermissionGranted(String[] permissions);

        void onPermissionDenied(String[] permissions);
    }
}
