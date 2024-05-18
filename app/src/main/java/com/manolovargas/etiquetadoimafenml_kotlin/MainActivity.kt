package com.manolovargas.etiquetadoimafenml_kotlin

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var Imagen: ImageView
    private lateinit var BtnEtiquetarImagen: Button
    private lateinit var Resultados: TextView

    private lateinit var imageLabeler: ImageLabeler

    private lateinit var progessDialog: Dialog

    var imageUri: Uri? = null


    private lateinit var translatorOptions: TranslatorOptions

    private lateinit var translator: Translator

    private val codigo_idioma_origen = "es"
    private val codigo_idioma_destino = "en"

    private var Texto_etiquetas = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        InicializarVistas()

        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        //val bitmapDrawable = Imagen.drawable as BitmapDrawable
        // val bitmap = bitmapDrawable.bitmap


        BtnEtiquetarImagen.setOnClickListener {
            //Toast.makeText(this, "Etiquetando Imagen", Toast.LENGTH_SHORT).show()
            // EtiquetarImagen(bitmap)

            if (imageUri != null) {

                EtiquetarImagenGaleria(imageUri!!)

            } else {
                Toast.makeText(this, "Por favor seleccione una imagen", Toast.LENGTH_SHORT).show()
            }


        }
    }


    private fun InicializarVistas() {
        Imagen = findViewById(R.id.Imagen)
        BtnEtiquetarImagen = findViewById(R.id.BtnEtiquetarImagen)
        Resultados = findViewById(R.id.Resultados)

        progessDialog = Dialog(this)
        progessDialog.setTitle("Espere, por favor")
        progessDialog.setCanceledOnTouchOutside(false)
    }

    private fun EtiquetarImagen(bitmap: Bitmap) {

        AlertDialog.Builder(this)
            .setMessage("Reconociendo objetos de la imagen ...")
            .setCancelable(false) // Para evitar que el diálogo se cierre al tocar fuera
            .setPositiveButton("Aceptar") { dialog, _ ->
                // Aquí puedes agregar el código para realizar el etiquetado de la imagen
                dialog.dismiss() // Cierra el diálogo cuando el usuario presiona el botón Aceptar
            }
            .show()

        val inputImagen = InputImage.fromBitmap(bitmap, 0)
        imageLabeler.process(inputImagen)
            .addOnSuccessListener { labels ->
                for (imageLabel in labels) {
                    /*Obtner la etiqueta gato, mono,panda,perro*/
                    val etiqueta = imageLabel.text
                    /*Obtener el porcentaje de confianza 92% 95% */
                    val confianza = imageLabel.confidence
                    /*Obtener el indice de la etiqueta*/

                    val indice = imageLabel.index

                    Resultados.append("Etiqueta: $etiqueta \n Confianza: $confianza \n Indice: $indice \n \n")
                }

                progessDialog.dismiss()
            }
            .addOnFailureListener { e ->
                progessDialog.dismiss()
                Toast.makeText(
                    applicationContext,
                    "No se puede realizar el etiquetado de la imagen por el error : ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }

    private fun EtiquetarImagenGaleria(imageUri: Uri) {

        AlertDialog.Builder(this)
            .setMessage("Reconociendo objetos de la imagen ...")
            .setCancelable(false) // Para evitar que el diálogo se cierre al tocar fuera
            .setPositiveButton("Aceptar") { dialog, _ ->
                // Aquí puedes agregar el código para realizar el etiquetado de la imagen
                dialog.dismiss() // Cierra el diálogo cuando el usuario presiona el botón Aceptar
            }
            .show()

        /*val inputImagen = InputImage.fromBitmap(bitmap, 0)*/

        var inputImage: InputImage? = null

        try {
            inputImage = InputImage.fromFilePath(applicationContext, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (inputImage != null) {

            imageLabeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    for (imageLabel in labels) {
                        /*Obtner la etiqueta gato, mono,panda,perro*/
                        val etiqueta = imageLabel.text
                        /*Obtener el porcentaje de confianza 92% 95% */
                        val confianza = imageLabel.confidence
                        /*Obtener el indice de la etiqueta*/

                        val indice = imageLabel.index

                        Resultados.append("||| Name : $etiqueta \n with a confidence of: $confianza \n and its index is : $indice \n \n")
                    }

                    progessDialog.dismiss()
                }
                .addOnFailureListener { e ->
                    progessDialog.dismiss()
                    Toast.makeText(
                        applicationContext,
                        "No se puede realizar el etiquetado de la imagen por el error : ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }


        }


    }

    private fun TraducirTexto() {

        Texto_etiquetas = Resultados.text.toString().trim()
        AlertDialog.Builder(this)
            .setMessage("Procesando...")
            .setCancelable(false) // Para evitar que el diálogo se cierre al tocar fuera
            .setPositiveButton("Aceptar") { dialog, _ ->
                // Aquí puedes agregar el código para realizar el etiquetado de la imagen
                dialog.dismiss() // Cierra el diálogo cuando el usuario presiona el botón Aceptar
            }
            .show()

        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(codigo_idioma_origen)
            .setTargetLanguage(codigo_idioma_destino)
            .build()

        translator = Translation.getClient(translatorOptions)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setMessage("Traduciendo Etiquetas...")

                translator.translate(Texto_etiquetas)
                    .addOnSuccessListener { etiquetasTraducidas ->
                        //Traduccion exitosa

                        progessDialog.dismiss()
                        Resultados.text = etiquetasTraducidas

                    }.addOnFailureListener { e ->
                        progessDialog.dismiss()
                        Toast.makeText(
                            applicationContext,
                            "No se puede realizar la traduccion por el error : ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { exception ->
                progessDialog.dismiss()
                Toast.makeText(
                    applicationContext,
                    "No se puede realizar la traduccion por el error : ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.mi_menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.MenuGaleria -> {
                Toast.makeText(this, "Abrir galeria", Toast.LENGTH_SHORT).show()

                SeleccionarImagenGaleria()
                true
            }

            R.id.MenuTraducir -> {
                Texto_etiquetas = Resultados.text.toString().trim()

                if (!Texto_etiquetas.isEmpty()) {//Si el texto no esta vacio

                    TraducirTexto()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "No existen etiquetas para traducir ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun SeleccionarImagenGaleria() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galeriaARL.launch(intent)

    }

    private val galeriaARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),

        ActivityResultCallback { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data
                Imagen.setImageURI(imageUri)
                Resultados.text = ""

            } else {
                Toast.makeText(applicationContext, "Cancelado por el usuario", Toast.LENGTH_SHORT)
                    .show()
            }

        })
}




