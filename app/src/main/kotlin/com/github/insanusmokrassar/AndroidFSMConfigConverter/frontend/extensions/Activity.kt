package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.extensions

import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat



fun Activity.getRealPathFromURI(contentUri: Uri): String? {
    val cursor = contentResolver.query(
            contentUri,
            arrayOf(MediaStore.Images.Media.DATA),
            null,
            null,
            null
    ) ?: return null
    cursor.moveToFirst()
    val res = cursor.getString(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    )
    cursor.close()
    return res
}

fun Activity.checkPermissions() {
    val notGrantedPermissions = ArrayList<String>()
    try {
        val info = packageManager
                .getPackageInfo(
                        packageName,
                        PackageManager.GET_PERMISSIONS
                )
        info.requestedPermissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                notGrantedPermissions.add(it)
            }
        }
        if (notGrantedPermissions.isEmpty()) {
            return
        }
        val needPermissions = notGrantedPermissions.toTypedArray()
        ActivityCompat.requestPermissions(
                this,
                needPermissions,
                0
        )
    } catch (e: PackageManager.NameNotFoundException) {
        throw IllegalStateException(
                "For some of reason activity package name is incorrect",
                e
        )
    }

}

