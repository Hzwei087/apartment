package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;

//    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);
        if (isUpdate){
            LambdaQueryWrapper<GraphInfo> graphInfoqueryWrapper = new LambdaQueryWrapper<>();
            graphInfoqueryWrapper.eq(GraphInfo::getItemId,roomSubmitVo.getId());
            graphInfoqueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoService.remove(graphInfoqueryWrapper);

            LambdaQueryWrapper<RoomPaymentType> roomPaymentTypequeryWrapper = new LambdaQueryWrapper<>();
            roomPaymentTypequeryWrapper.eq(RoomPaymentType::getRoomId,roomSubmitVo.getId());
            roomPaymentTypeService.remove(roomPaymentTypequeryWrapper);

            LambdaQueryWrapper<RoomAttrValue> roomAttrValuequeryWrapper = new LambdaQueryWrapper<>();
            roomAttrValuequeryWrapper.eq(RoomAttrValue::getRoomId,roomSubmitVo.getId());
            roomAttrValueService.remove(roomAttrValuequeryWrapper);

            LambdaQueryWrapper<RoomLabel> roomLabelqueryWrapper = new LambdaQueryWrapper<>();
            roomLabelqueryWrapper.eq(RoomLabel::getRoomId,roomSubmitVo.getId());
            roomLabelService.remove(roomLabelqueryWrapper);

            LambdaQueryWrapper<RoomFacility> roomFacilityqueryWrapper = new LambdaQueryWrapper<>();
            roomFacilityqueryWrapper.eq(RoomFacility::getRoomId,roomSubmitVo.getId());
            roomFacilityService.remove(roomFacilityqueryWrapper);

            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermqueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermqueryWrapper.eq(RoomLeaseTerm::getRoomId,roomSubmitVo.getId());
            roomLeaseTermService.remove(roomLeaseTermqueryWrapper);
            //删除缓存
            String key = RedisConstant.APP_ROOM_PREFIX + roomSubmitVo.getId();
            redisTemplate.delete(key);
        }

        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo:graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setName(graphVo.getName());
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
        List<Long> attrValueIdList = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueIdList)){
            ArrayList<RoomAttrValue> roomAttrValueList = new ArrayList<>();
            for (Long id : attrValueIdList) {
                RoomAttrValue roomAttrValue = RoomAttrValue.builder()
                        .roomId(roomSubmitVo.getId())
                        .attrValueId(id).build();
                roomAttrValueList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueList);
        }

        List<Long> facilityInfoIdList = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIdList)){
            ArrayList<RoomFacility> facilityInfoList = new ArrayList<>();
            for(Long id : facilityInfoIdList){
                RoomFacility roomFacility = RoomFacility.builder()
                        .roomId(roomSubmitVo.getId())
                        .facilityId(id)
                        .build();
                facilityInfoList.add(roomFacility);
            }
            roomFacilityService.saveBatch(facilityInfoList);

        }
        List<Long> labelInfoIdList = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(labelInfoIdList)){
            ArrayList<RoomLabel> roomLabelList = new ArrayList<>();
            for (Long id : labelInfoIdList){
                RoomLabel roomLabel = RoomLabel.builder()
                        .roomId(roomSubmitVo.getId())
                        .labelId(id)
                        .build();
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }

        List<Long> paymentTypeIdList = roomSubmitVo.getPaymentTypeIds();
        if(!CollectionUtils.isEmpty(paymentTypeIdList)){
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for (Long id : paymentTypeIdList){
                RoomPaymentType roomPaymentType = RoomPaymentType.builder()
                        .roomId(roomSubmitVo.getId())
                        .paymentTypeId(id)
                        .build();
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }

        List<Long> leaseTermIdList = roomSubmitVo.getLeaseTermIds();
        if(!CollectionUtils.isEmpty(leaseTermIdList)){
            ArrayList<RoomLeaseTerm> roomLeaseTermList = new ArrayList<>();
            for(Long id : leaseTermIdList){
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder()
                        .roomId(roomSubmitVo.getId())
                        .leaseTermId(id)
                        .build();
                roomLeaseTermList.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTermList);
        }


    }

    @Override
    public IPage<RoomItemVo> pageItemByQuery(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItemByQuery(page,queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo = super.getById(id);

        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());

        List<GraphVo> graphVoList = graphInfoMapper.selectByItemTypeAndId(ItemType.ROOM, id);

        List<AttrValueVo> attrValueVoList = attrValueMapper.selectListByRoomId(id);

        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);

        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);

        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo,roomDetailVo);
        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(graphVoList);
        roomDetailVo.setAttrValueVoList(attrValueVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);

        return roomDetailVo;
    }

    @Override
    public void removeRoomAllByid(Long id) {
        super.removeById(id);

        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermqueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermqueryWrapper.eq(RoomLeaseTerm::getRoomId,id);
        roomLeaseTermService.remove(roomLeaseTermqueryWrapper);

        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypequeryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypequeryWrapper.eq(RoomPaymentType::getRoomId,id);
        roomPaymentTypeService.remove(roomPaymentTypequeryWrapper);

        LambdaQueryWrapper<RoomAttrValue> roomAttrValuequeryWrapper = new LambdaQueryWrapper<>();
        roomAttrValuequeryWrapper.eq(RoomAttrValue::getRoomId,id);
        roomAttrValueService.remove(roomAttrValuequeryWrapper);

        LambdaQueryWrapper<RoomLabel> roomLabelqueryWrapper = new LambdaQueryWrapper<>();
        roomLabelqueryWrapper.eq(RoomLabel::getRoomId,id);
        roomLabelService.remove(roomLabelqueryWrapper);

        LambdaQueryWrapper<RoomFacility> roomFacilityqueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityqueryWrapper.eq(RoomFacility::getRoomId,id);
        roomFacilityService.remove(roomFacilityqueryWrapper);

        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoQueryWrapper.eq(GraphInfo::getItemId,ItemType.ROOM);
        graphInfoService.remove(graphInfoQueryWrapper);

        //删除缓存
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        redisTemplate.delete(key);
    }
}




