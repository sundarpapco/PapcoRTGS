package com.papco.sundar.papcortgs.extentions

import android.Manifest
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R

fun Fragment.updateSubTitle(subTitle: String) {

    try {
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = subTitle
    } catch (_: Exception) {

    }
}

fun Fragment.updateTitle(title: String) {

    try {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    } catch (_: Exception) {

    }
}

fun Fragment.registerBackArrowMenu(
    onBack:()->Unit ={
        findNavController().popBackStack()
    }
){
    val menuProvider = object: MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (menuItem.itemId == android.R.id.home) {
                onBack()
                return true
            }

            return false
        }
    }

    requireActivity().addMenuProvider(menuProvider,viewLifecycleOwner)
}

fun Fragment.getActionBar(): ActionBar? {
    return try {
        (requireActivity() as AppCompatActivity).supportActionBar
    } catch (e: Exception) {
        null
    }
}

fun Fragment.enableBackArrow() {
    getActionBar()?.setDisplayHomeAsUpEnabled(true)
    getActionBar()?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
}

fun Fragment.disableBackArrow() {
    getActionBar()?.setDisplayHomeAsUpEnabled(false)
}

