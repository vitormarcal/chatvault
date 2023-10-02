package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.input.AttachmentCriteriaInput
import org.springframework.core.io.Resource

interface AttachmentFinder {

    fun execute(criteriaInput: AttachmentCriteriaInput): Resource

}