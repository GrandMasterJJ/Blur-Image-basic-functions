package com.example.bluromatic.workers

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {


    override suspend fun doWork(): Result {
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)


        // display a status notification and notify the user that the blur worker is blurring the image
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        // A CoroutineWorker, by default,runs as Dispatchers.
        // Default but can be changed by calling withContext()
        // and passing in the desired dispatcher.
        return withContext(Dispatchers.IO) {

            // a function that is added to emulate slower work
            delay(DELAY_TIME_MILLIS)

            // where the actual blur image work

            //  the require() statement which throws an IllegalArgumentException if the first argument evaluates to false.
            return@withContext try { // use return@withContext : because we cannot return a function within a lambda function
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                        Log.e(TAG, errorMessage)
                        errorMessage
                }
                // the resource image source is passed in as a URI,
                // we need a ContentResolver object to read the contents
                // pointed by the URI
                val resolver = applicationContext.contentResolver

                // pass in the picture
                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )

                // pass in the picture
                /*
                val picture = BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.drawable.android_cupcake
                )

                 */

                // blur the image
                val output = blurBitmap(picture, blurLevel)

                // write bitmap to a temp file
                val outputUri = writeBitmapToFile(applicationContext, output)


                makeStatusNotification(
                    "Output is $outputUri",
                    applicationContext
                )


                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
                Result.success(outputData)

            }catch (throwable: Throwable){
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )
                Result.failure()
            }
        }


    }

}
