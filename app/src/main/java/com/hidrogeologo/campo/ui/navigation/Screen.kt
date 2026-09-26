package com.hidrogeologo.campo.ui.navigation

sealed class Screen(val route: String) {
    data object RecordList : Screen("record_list")

    data object RecordForm : Screen("record_form/{recordId}") {
        const val ARG_RECORD_ID = "recordId"
        const val NEW_RECORD_ID = -1L
        fun createRoute(recordId: Long = NEW_RECORD_ID) = "record_form/$recordId"
    }

    data object RecordDetail : Screen("record_detail/{recordId}") {
        const val ARG_RECORD_ID = "recordId"
        fun createRoute(recordId: Long) = "record_detail/$recordId"
    }
}
