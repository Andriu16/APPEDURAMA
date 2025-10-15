package com.example.appedurama

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.appedurama.data.datasource.GeminiApiService
import com.example.appedurama.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        lifecycleScope.launch {
//            GeminiApiService.listarModelosDisponibles()
//        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_perfil
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Usamos un when para decidir qué hacer basado en el ID del fragmento de destino.
            when (destination.id) {

                // Para Home y Notifications, SÓLO mostramos la barra de navegación inferior.
                // La barra superior (ActionBar) es controlada por cada fragmento individualmente.
                R.id.navigation_home,
                R.id.navigation_notifications,
                R.id.navigation_dashboard,
                R.id.navigation_perfil -> { // <-- ¡Añadido aquí!
                    binding.navView.visibility = View.VISIBLE
                    supportActionBar?.hide() // <-- ¡Esta es la línea clave!
                }

                R.id.cursosFragment -> {
                    binding.navView.visibility = View.GONE   // Ocultar barra de navegación inferior
                    supportActionBar?.show()                 // MOSTRAR barra superior
                }

                // Para cualquier otro fragmento (como login), ocultamos ambas barras.
                else -> {
                    binding.navView.visibility = View.GONE
                    supportActionBar?.hide()
                }
            }
        }
    }
}