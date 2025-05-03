package com.mckimquyen.atomicPeriodicTable

import android.app.Application
import android.widget.Toast

//TODO roy93~ firebase

//done
//review in app bingo
//120hz
//rate app
//more app
//share app
//policy
//manifest ad id
//leak canary
//ic launcher
//splash screen xml
//keystore
//github 20 tester
//ad applovin
//font scale

//done
class RoyApp : Application() {
    override fun onCreate() {
        super.onCreate()
//        this.setupApplovinAd()
        //TODO roy93~ admob init
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "$packageName onCreate", Toast.LENGTH_SHORT).show()
        }
    }
}
