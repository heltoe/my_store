package com.example.my_store.courier.service;

import com.example.my_store.courier.controller.dto.CreateUpdateCourierDto;
import com.example.my_store.courier.controller.dto.GetCourierDto;
import com.example.my_store.courier.repository.CourierRepository;
import com.example.my_store.courier.repository.entity.CourierEntity;
import com.example.my_store.courier.utils.CourierEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CourierServiceImpl implements CourierService {

    private final CourierEntityMapper courierEntityMapper;

    private final CourierRepository courierRepository;

    private CourierEntity _getOne(Long id) {
        return  courierRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @Override
    public Page<GetCourierDto> getAll(Pageable pageable) {
        Page<CourierEntity> courierEntities = courierRepository.findAll(pageable);
        return courierEntities.map(courierEntityMapper::convertToGetCourierDto);
    }

    @Override
    public GetCourierDto getOne(Long id) {
        CourierEntity entity = _getOne(id);
        return courierEntityMapper.convertToGetCourierDto(entity);
    }

    @Override
    public List<GetCourierDto> getMany(List<Long> ids) {
        List<CourierEntity> courierEntities = courierRepository.findAllById(ids);
        return courierEntities.stream()
                .map(courierEntityMapper::convertToGetCourierDto)
                .toList();
    }

    @Override
    public GetCourierDto create(CreateUpdateCourierDto dto) {
        CourierEntity courierEntity = courierEntityMapper.convertToEntity(dto);
        CourierEntity resultCourierEntity = courierRepository.save(courierEntity);
        return courierEntityMapper.convertToGetCourierDto(resultCourierEntity);
    }

    @Override
    public GetCourierDto patch(Long id, CreateUpdateCourierDto dto) {
        CourierEntity entity = _getOne(id);

        courierEntityMapper.updateWithNull(dto, entity);

        CourierEntity resultCourierEntity = courierRepository.save(entity);
        return courierEntityMapper.convertToGetCourierDto(resultCourierEntity);
    }

    @Override
    public void delete(Long id) {
        CourierEntity entity = _getOne(id);
        if (entity != null) {
            courierRepository.delete(entity);
        }
    }

    @Override
    public void deleteMany(List<Long> ids) {
        courierRepository.deleteAllById(ids);
    }
}
