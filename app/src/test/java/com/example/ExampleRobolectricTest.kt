package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.generator.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QR Maker", appName)
    }

    @Test
    fun `generate QR bitmatrix and bitmap`() {
        val matrix = QrCodeGenerator.generateBitMatrix("https://google.com")
        assertNotNull(matrix)
        val bitmap = QrCodeGenerator.renderBitmap(matrix = matrix!!)
        assertNotNull(bitmap)
        assertEquals(1024, bitmap.width)
        assertEquals(1024, bitmap.height)
    }
}
