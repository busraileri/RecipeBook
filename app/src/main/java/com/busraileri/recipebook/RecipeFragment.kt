package com.busraileri.recipebook

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream


class RecipeFragment : Fragment() {

    var selectedImage: Uri? = null
    var selectedBitmap: Bitmap? = null
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val button: Button = view.findViewById(R.id.button)
        imageView = view.findViewById(R.id.imageView)

        button.setOnClickListener {
            save()
        }

        imageView.setOnClickListener {
            selectImg()
        }

        arguments?.let {
            val receivedInfo = RecipeFragmentArgs.fromBundle(it).information

            if(receivedInfo == "frommenu"){
                //for new recipe
                val foodNameText: TextView = requireView().findViewById(R.id.foodNameText)
                foodNameText.text = ""

                val recipeText: TextView = requireView().findViewById(R.id.recipeText)
                recipeText.text = ""

                button.visibility = View.VISIBLE

                val selectImageBackground = BitmapFactory.decodeResource(context?.resources, R.drawable.select_img)
                imageView.setImageBitmap(selectImageBackground)

            }else{
                //for created recipe

                button.visibility = View.INVISIBLE

                val selectedId = RecipeFragmentArgs.fromBundle(it).id

                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                        val cursor = db.rawQuery("SELECT * FROM foods WHERE id= ?", arrayOf(selectedId.toString()))

                        val IndexOfFoodName = cursor.getColumnIndex("foodname")
                        val IndexOfRecipe = cursor.getColumnIndex("recipe")
                        val foodImage = cursor.getColumnIndex("image")

                        while(cursor.moveToNext()){
                            val foodNameText: TextView = requireView().findViewById(R.id.foodNameText)
                            val recipeText: TextView = requireView().findViewById(R.id.recipeText)

                            foodNameText.setText(cursor.getString(IndexOfFoodName))
                            recipeText.setText(cursor.getString(IndexOfRecipe))

                            val byteArray = cursor.getBlob(foodImage)
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0 , byteArray.size)
                            imageView.setImageBitmap(bitmap)



                        }

                        cursor.close()


                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private fun save() {
        val foodNameText: TextView = requireView().findViewById(R.id.foodNameText)
        val foodName = foodNameText.text.toString()

        val recipeText: TextView = requireView().findViewById(R.id.recipeText)
        val recipe = recipeText.text.toString()

        if (selectedBitmap != null) {
            val smallBitmap = sizeReductionBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS foods (id INTEGER PRIMARY KEY, foodname VARCHAR, recipe VARCHAR, image BLOB)")

                    val sqlString = "INSERT INTO foods (foodname, recipe, image) VALUES (?,?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1, foodName)
                    statement.bindString(2, recipe)
                    statement.bindBlob(3, byteArray)
                    statement.execute()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val action = RecipeFragmentDirections.actionRecipeFragmentToListFragment()
            view?.let {
                Navigation.findNavController(it).navigate(action)
            }
        }
    }

    private fun selectImg() {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImage = data.data

            try {
                context?.let {
                    if (selectedImage != null) {
                        selectedBitmap = if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(it.contentResolver, selectedImage!!)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(it.contentResolver, selectedImage)
                        }
                        imageView.setImageBitmap(selectedBitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sizeReductionBitmap(userSelectedBitmap: Bitmap, maximumSize: Int): Bitmap {
        var width = userSelectedBitmap.width
        var height = userSelectedBitmap.height

        val bitmapRate: Double = width.toDouble() / height.toDouble()
        if (bitmapRate > 1) {
            width = maximumSize
            val shortenedHeight = width / bitmapRate
            height = shortenedHeight.toInt()
        } else {
            height = maximumSize
            val shortenedWidth = height * bitmapRate
            width = shortenedWidth.toInt()
        }

        return Bitmap.createScaledBitmap(userSelectedBitmap, width, height, true)
    }
}
