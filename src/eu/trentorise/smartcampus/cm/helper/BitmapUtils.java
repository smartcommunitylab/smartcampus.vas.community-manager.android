/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.cm.helper;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class BitmapUtils {

	private static Bitmap rotateBitmap(Bitmap img, String absPath) throws IOException {
		Matrix matrix = new Matrix();
		ExifInterface exifReader = new ExifInterface(absPath);
		int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
		if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
		       matrix.postRotate(90);
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
		       matrix.postRotate(180);
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
		      matrix.postRotate(270);
		}
		if (orientation != ExifInterface.ORIENTATION_NORMAL) {
			img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
		}
		return img;
	}

	public static String getBitmapAbsolutePath(Context ctx, Uri imageUri) {
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = ctx.getContentResolver().query(imageUri,
				filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;
	}

	public static Bitmap scale(Context ctx, Uri imageUri, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		String imagePath = getBitmapAbsolutePath(ctx, imageUri);

		BitmapFactory.decodeFile(imagePath, options);

		int widthRatio = options.outWidth / width;
		int heightRatio = options.outHeight / height;

		int sample = (widthRatio < heightRatio) ? widthRatio : heightRatio;

		options.inJustDecodeBounds = false;
		options.inSampleSize = sample;

		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
		if (bitmap != null) {
			try {
				bitmap = rotateBitmap(bitmap, imagePath);
			} catch (IOException e) {
				Log.w(BitmapUtils.class.getSimpleName(), "Problem rotating image: "+e.getMessage());
			}
			bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
			return bitmap;
		} else {
			return null;
		}
	}
}
