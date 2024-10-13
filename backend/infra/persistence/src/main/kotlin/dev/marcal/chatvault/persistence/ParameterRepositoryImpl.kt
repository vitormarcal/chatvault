package dev.marcal.chatvault.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import dev.marcal.chatvault.model.Parameter
import dev.marcal.chatvault.persistence.entity.ParameterEntity
import dev.marcal.chatvault.persistence.repository.ParameterCrudRepository
import dev.marcal.chatvault.repository.ParameterRepository
import org.springframework.stereotype.Service

@Service
class ParameterRepositoryImpl(
    private val objectMapper: ObjectMapper,
    private val parameterCrudRepository: ParameterCrudRepository
): ParameterRepository {

    override fun findAll(): List<Parameter> =
        parameterCrudRepository.findAll().map {
            Parameter(it.name, it.config ?: "")
        }

    override fun save(parameter: Parameter): Parameter {
        parameterCrudRepository.save(ParameterEntity(parameter.name, parameter.value))
        return parameter
    }

}