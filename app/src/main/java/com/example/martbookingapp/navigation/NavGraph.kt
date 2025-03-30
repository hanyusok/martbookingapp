package com.example.martbookingapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.martbookingapp.ui.screen.AddAppointmentScreen
import com.example.martbookingapp.ui.screen.AddEditPatientScreen
import com.example.martbookingapp.ui.screen.AppointmentsScreen
import com.example.martbookingapp.ui.screen.EditAppointmentScreen
import com.example.martbookingapp.ui.screen.PatientsScreen

sealed class Screen(val route: String) {
    data object Appointments : Screen("appointments")
    data object AddAppointment : Screen("add_appointment")
    data object EditAppointment : Screen("edit_appointment/{appointmentId}") {
        fun createRoute(appointmentId: String) = "edit_appointment/$appointmentId"
    }
    data object Patients : Screen("patients")
    data object AddPatient : Screen("add_patient")
    data object EditPatient : Screen("edit_patient/{patientId}") {
        fun createRoute(patientId: String) = "edit_patient/$patientId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Appointments.route,
        modifier = modifier
    ) {
        composable(Screen.Appointments.route) {
            AppointmentsScreen(
                onNavigateToAddAppointment = {
                    navController.navigate(Screen.AddAppointment.route)
                },
                onNavigateToEditAppointment = { appointmentId: String ->
                    navController.navigate(Screen.EditAppointment.createRoute(appointmentId))
                }
            )
        }

        composable(Screen.AddAppointment.route) {
            AddAppointmentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppointmentCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditAppointment.route,
            arguments = listOf(
                navArgument("appointmentId") {
                    type = NavType.StringType
                    nullable = false
                    defaultValue = ""
                }
            )
        ) {
            val appointmentId = it.arguments?.getString("appointmentId") ?: return@composable
            EditAppointmentScreen(
                appointmentId = appointmentId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppointmentUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Patients.route) {
            PatientsScreen(
                onNavigateToAddPatient = {
                    navController.navigate(Screen.AddPatient.route)
                },
                onNavigateToEditPatient = { patientId: String ->
                    navController.navigate(Screen.EditPatient.createRoute(patientId))
                }
            )
        }

        composable(Screen.AddPatient.route) {
            AddEditPatientScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPatientSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditPatient.route,
            arguments = listOf(
                navArgument("patientId") {
                    type = NavType.StringType
                    nullable = false
                    defaultValue = ""
                }
            )
        ) {
            val patientId = it.arguments?.getString("patientId") ?: return@composable
            AddEditPatientScreen(
                patientId = patientId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPatientSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
} 