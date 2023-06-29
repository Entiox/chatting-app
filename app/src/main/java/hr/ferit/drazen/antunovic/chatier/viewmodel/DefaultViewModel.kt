package hr.ferit.drazen.antunovic.chatier.viewmodel

import androidx.lifecycle.ViewModel
import hr.ferit.drazen.antunovic.chatier.repository.Repository
import hr.ferit.drazen.antunovic.chatier.repository.RepositoryImpl

abstract class DefaultViewModel(protected val repository: Repository = RepositoryImpl()) : ViewModel() {
    abstract fun refresh()
}
