package dev.marcal.chatvault.repository

import dev.marcal.chatvault.model.Parameter

interface ParameterRepository {

    fun findAll(): List<Parameter>
    fun save(parameter: Parameter): Parameter
}