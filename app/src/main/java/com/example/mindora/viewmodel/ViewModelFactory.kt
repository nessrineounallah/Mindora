package com.example.mindora.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mindora.data.repository.StudyGoalRepository
import com.example.mindora.data.repository.StudentProfileRepository
import com.example.mindora.data.repository.StudyActivityRepository
import com.example.mindora.data.repository.SubjectRepository
import com.example.mindora.data.repository.TaskRepository
import com.example.mindora.ai.data.LiteRtLmLocalAiRepository
import com.example.mindora.ai.viewmodel.LocalAiViewModel

class ViewModelFactory(
    private val application: Application,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val studyGoalRepository: StudyGoalRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val studyActivityRepository: StudyActivityRepository,
    private val localAiRepository: LiteRtLmLocalAiRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(subjectRepository, taskRepository, studentProfileRepository, studyActivityRepository) as T
            }
            modelClass.isAssignableFrom(SubjectViewModel::class.java) -> {
                SubjectViewModel(subjectRepository, taskRepository) as T
            }
            modelClass.isAssignableFrom(TaskViewModel::class.java) -> {
                TaskViewModel(application, taskRepository, subjectRepository) as T
            }
            modelClass.isAssignableFrom(StudentProfileViewModel::class.java) -> {
                StudentProfileViewModel(studentProfileRepository) as T
            }
            modelClass.isAssignableFrom(ProgressViewModel::class.java) -> {
                ProgressViewModel(taskRepository, studyGoalRepository, studyActivityRepository) as T
            }
            modelClass.isAssignableFrom(LocalAiViewModel::class.java) -> {
                LocalAiViewModel(application, localAiRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
