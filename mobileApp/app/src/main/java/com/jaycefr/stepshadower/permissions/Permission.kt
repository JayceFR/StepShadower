package com.jaycefr.stepshadower.permissions

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.jaycefr.stepshadower.AdminReceiver
import com.jaycefr.stepshadower.R

data class Permission(
    val title: String,
    val description: String,
    val permission: String?,
    val imageRes: Int,
    val routeName : String,
    var nextRoute : String? = null,
    val deviceAdmin : Boolean = false
)

val permissions : List<Permission> = listOf(
    Permission(
        title = "Device Admin Access",
        description = "We need device admin to be able to monitor failed unlocks",
        permission = null,
        imageRes = R.raw.device_admin,
        routeName = "device_admin",
        deviceAdmin = true
    ),
    Permission(
        title = "Camera Access",
        description = "We use your camera to take a picture of intruders.",
        permission = Manifest.permission.CAMERA,
        imageRes = R.raw.camera,
        routeName = "camera"
    ),
    Permission(
        title = "Notification Access",
        description = "We use notifications to alert you when someone tries to break in.",
        permission = Manifest.permission.POST_NOTIFICATIONS,
        imageRes = R.raw.notification,
        routeName = "notifications"
    ),
    Permission(
        title = "Location Access",
        description = "Location helps us tag where the unauthorized attempt occurred.",
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        imageRes = R.raw.location,
        routeName = "location"
    ),
    Permission(
        title = "Background location Access",
        description = "Location helps us tag where the unauthorized attempt occurred.",
        permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        imageRes = R.raw.background_loc,
        routeName = "background_location"
    )
)

fun buildRequiredPermissionList(context : Context, nextRoute : String? = null) : List<Permission>{
    val returnList = mutableListOf<Permission>()

    for (permission in permissions){
        if (permission.deviceAdmin){
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(context, AdminReceiver::class.java)
            if (!dpm.isAdminActive(adminComponent)) {
                if (returnList.isNotEmpty()){
                    returnList.last().nextRoute = permission.routeName
                }
                returnList.add(permission.copy())
            }
        }
        else{
            if (permission.permission!! == Manifest.permission.ACCESS_BACKGROUND_LOCATION){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    if (ContextCompat.checkSelfPermission(context, permission.permission!!) != PackageManager.PERMISSION_GRANTED){
                        if (returnList.isNotEmpty()){
                            returnList.last().nextRoute = permission.routeName
                        }
                        returnList.add(permission.copy())
                    }
                }
            }
            else{
                if (ContextCompat.checkSelfPermission(context, permission.permission!!) != PackageManager.PERMISSION_GRANTED){
                    if (returnList.isNotEmpty()){
                        returnList.last().nextRoute = permission.routeName
                    }
                    returnList.add(permission.copy())
                }
            }
        }
    }

    if (returnList.isNotEmpty()){
        returnList.last().nextRoute = nextRoute
    }

    return returnList
}
