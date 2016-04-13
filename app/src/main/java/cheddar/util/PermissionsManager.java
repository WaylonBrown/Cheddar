package cheddar.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by waylon.brown on 4/11/16.
 */
public class PermissionsManager {
    private List<String> permissionsList;
    private Activity activity;

    PermissionsManager(Activity activity, List<String> permissionsList) {
        this.activity = activity;
        this.permissionsList = permissionsList;
    }

    public void ask(final int requestCode) {
        if (permissionsList != null && permissionsList.size() > 0) {
            List<String> permsRequired = new ArrayList<>();
            for (String permission : permissionsList) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permsRequired.add(permission);
                }
            }

            if (permsRequired.size() > 0) {
                String[] permsReq = new String[permsRequired.size()];
                permsReq = permsRequired.toArray(permsReq);
                ActivityCompat.requestPermissions(activity, permsReq, requestCode);
            }
        }
    }

    public boolean allPermissionsGranted() {
        if (permissionsList != null && permissionsList.size() > 0) {
            for (String permission : permissionsList) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static class Builder {
        private List<String> permissionsList;
        private Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder add(String permission) {
            if (permissionsList == null) {
                permissionsList = new ArrayList<>();
            }
            permissionsList.add(permission);

            return this;
        }

        public PermissionsManager build() {
            return new PermissionsManager(activity, permissionsList);
        }
    }
}
