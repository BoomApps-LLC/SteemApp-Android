package com.boomapps.steemapp.main

/**
 * Created by Vitali Grechikha on 04.02.2018.
 */
data class Balance(
        val steemBalance: Double = 0.0,
        val steemSavingBalance: Double = 0.0,
        val sbdBalance: Double = 0.0,
        val sbdSavingBalance: Double = 0.0,
        val vestShares: Double = 0.0,
        val fullBalance: Double = 0.0,
        val updateTime: Long = 0L
)