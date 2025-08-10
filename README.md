# Barebones Fitness Application - WorkoutLog <img src="docs/fitness_app_icon.png" alt="App Icon" width="80" style="vertical-align:middle; margin-right:10px;" /> 





This is a fitness application made using Android Studio, and Jetpack Compose. See the full planning document [here](docs/planning_document.pdf), and the final reflection document [here](docs/reflection.pdf). A key feature of this application is its ability so sync with [Android Health Connect](https://health.google/health-connect-android/).

Below is a view of the primary screen in the application

![Light Mode UI Preview](docs/light_mode_ui_preview.png)
![Dark Mode UI Preview](docs/dark_mode_ui_preview.png)


## Database Design

This application uses a structured database to efficiently manage workouts and exercises.

- **Database Overview:**  
    ![Explanation of the nested database](docs/explanatory_database_figure.png)

- **Table Structure:**  
    The following diagram illustrates the Room database tables. All tables except `ExerciseVariation` have been implemented.  
    ![Table Structure](docs/room_structure.png)


