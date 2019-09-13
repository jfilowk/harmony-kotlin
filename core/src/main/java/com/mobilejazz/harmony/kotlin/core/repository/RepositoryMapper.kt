package com.mobilejazz.harmony.kotlin.core.repository

import com.mobilejazz.harmony.kotlin.core.repository.mapper.Mapper
import com.mobilejazz.harmony.kotlin.core.repository.mapper.map
import com.mobilejazz.harmony.kotlin.core.repository.operation.Operation
import com.mobilejazz.harmony.kotlin.core.repository.query.Query
import javax.inject.Inject


/**
 * This repository uses mappers to map objects and redirects them to the contained repository, acting as a simple "translator".
 *
 * @param getRepository Repository with get operations
 * @param putRepository Repository with put operations
 * @param deleteRepository Repository with delete operations
 * @param toOutMapper Mapper to map data objects to domain objects
 * @param toInMapper Mapper to map domain objects to data objects
 */
class RepositoryMapper<In, Out> @Inject constructor(
    private val getRepository: GetRepository<In>,
    private val putRepository: PutRepository<In>,
    private val deleteRepository: DeleteRepository,
    private val toOutMapper: Mapper<In, Out>,
    private val toInMapper: Mapper<Out, In>
) : GetRepository<Out>, PutRepository<Out>, DeleteRepository {

  override suspend fun get(query: Query, operation: Operation): Out = getRepository.get(query, operation).let { toOutMapper.map(it) }

  override suspend fun getAll(query: Query, operation: Operation): List<Out> = getRepository.getAll(query, operation).map { toOutMapper.map(it) }

  override suspend fun put(query: Query, value: Out?, operation: Operation): Out {
    val mapped = value?.let { toInMapper.map(it) }
    return putRepository.put(query, mapped, operation).let {
      toOutMapper.map(it)
    }
  }

  override suspend fun putAll(query: Query, value: List<Out>?, operation: Operation): List<Out> {
    val mapped = value?.let { toInMapper.map(value) }
    return putRepository.putAll(query, mapped, operation).map { toOutMapper.map(it) }
  }

  override suspend fun delete(query: Query, operation: Operation) = deleteRepository.delete(query, operation)

  override suspend fun deleteAll(query: Query, operation: Operation) = deleteRepository.deleteAll(query, operation)

}

class GetRepositoryMapper<In, Out> @Inject constructor(
    private val getRepository: GetRepository<In>,
    private val toOutMapper: Mapper<In, Out>
) : GetRepository<Out> {

  override suspend fun get(query: Query, operation: Operation): Out = getRepository.get(query, operation).let { toOutMapper.map(it) }

  override suspend fun getAll(query: Query, operation: Operation): List<Out> = getRepository.getAll(query, operation).map { toOutMapper.map(it) }
}

class PutRepositoryMapper<In, Out> @Inject constructor(
    private val putRepository: PutRepository<In>,
    private val toOutMapper: Mapper<In, Out>,
    private val toInMapper: Mapper<Out, In>) : PutRepository<Out> {

  override suspend fun put(query: Query, value: Out?, operation: Operation): Out {
    val mapped = value?.let { toInMapper.map(it) }
    return putRepository.put(query, mapped, operation).let {
      toOutMapper.map(it)
    }
  }

  override suspend fun putAll(query: Query, value: List<Out>?, operation: Operation): List<Out> {
    val mapped = value?.let { toInMapper.map(value) }
    return putRepository.putAll(query, mapped, operation).map { toOutMapper.map(it) }
  }
}