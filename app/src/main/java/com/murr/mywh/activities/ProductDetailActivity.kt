package com.murr.mywh.activities

import android.content.Context
import android.content.Intent

// ProductDetailActivity больше не используется, так как продукты объединены с папками
// Класс оставлен для совместимости

class ProductDetailActivity : androidx.appcompat.app.AppCompatActivity() {
    companion object {
        fun newIntent(context: Context, productId: Long) =
            Intent(context, ProductDetailActivity::class.java)
    }
}

