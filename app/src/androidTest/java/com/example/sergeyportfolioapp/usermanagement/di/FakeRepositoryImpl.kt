package com.example.sergeyportfolioapp.usermanagement.di

import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.google.firebase.auth.FirebaseAuthException

class FakeRepositoryImpl : Repository {
    override suspend fun loginUserAndReturnName(email: String, password: String): String {
        if((email.equals("test@gmail.com")) && (password.equals("test123"))){
            return "test@gmail.com"
        }
        throw FirebaseAuthException("no user","no_user")
    }

}
