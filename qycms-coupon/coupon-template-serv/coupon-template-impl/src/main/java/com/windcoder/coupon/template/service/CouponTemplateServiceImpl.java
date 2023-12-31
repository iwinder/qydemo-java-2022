package com.windcoder.coupon.template.service;

import com.windcoder.coupon.template.api.beans.CouponTemplateInfo;
import com.windcoder.coupon.template.api.beans.PagedCouponTemplateInfo;
import com.windcoder.coupon.template.api.beans.TemplateSearchParams;
import com.windcoder.coupon.template.api.enums.CouponType;
import com.windcoder.coupon.template.converter.CouponTemplateConverter;
import com.windcoder.coupon.template.dao.CouponTemplateDao;
import com.windcoder.coupon.template.dao.entity.CouponTemplate;
import com.windcoder.coupon.template.service.intf.CouponTemplateService;
import com.windcoder.coupon.template.service.intf.CouponTemplateServiceTCC;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponTemplateServiceImpl implements CouponTemplateServiceTCC {

    @Autowired
    private CouponTemplateDao templateDao;

    @Override
    public CouponTemplateInfo createTemplate(CouponTemplateInfo request) {
        // 单个门店最多可以创建100张优惠券模板
        if(request.getShopId()!=null) {
            Integer count = templateDao.countByShopIdAndAvailable(request.getShopId(),true);
            if (count>=100) {
                log.error("the totals of coupon template exceeds maximum number");
                throw new UnsupportedOperationException("exceeded the maximum of coupon templates that you can create");
            }
        }


        CouponTemplate template = CouponTemplate.builder()
                .name(request.getName())
                .description(request.getDesc())
                .category(CouponType.convert(request.getType()))
                .available(true)
                .shopId(request.getShopId())
                .rule(request.getRule())
                .build();

        template = templateDao.save(template);

        return CouponTemplateConverter.convertToTemplateInfo(template);
    }

    /**
     * 克隆优惠券
     * @param templateId
     * @return
     */
    @Override
    public CouponTemplateInfo cloneTemplate(Long templateId) {
        log.info("cloning template id {}", templateId);


        CouponTemplate source = templateDao.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("invalid template ID"));

        CouponTemplate target = new CouponTemplate();
        BeanUtils.copyProperties(source,target);

        target.setAvailable(true);
        target.setId(null);

        templateDao.save(target);
        return CouponTemplateConverter.convertToTemplateInfo(target);
    }

    @Override
    public PagedCouponTemplateInfo search(TemplateSearchParams request) {
        CouponTemplate example = CouponTemplate.builder()
                .shopId(request.getShopId())
                .category(CouponType.convert(request.getType()))
                .available(request.getAvailable())
                .name(request.getName())
                .build();

        Pageable page = PageRequest.of(request.getPage(), request.getPageSize());
        templateDao.findAll(Example.of(example), page);

        Page<CouponTemplate> result = templateDao.findAll(Example.of(example), page);
        List<CouponTemplateInfo> couponTemplateInfos = result.stream()
                .map(CouponTemplateConverter::convertToTemplateInfo)
                .collect(Collectors.toList());

        PagedCouponTemplateInfo response = PagedCouponTemplateInfo.builder()
                .templates(couponTemplateInfos)
                .page(request.getPage())
                .total(result.getTotalElements())
                .build();

        return response;
    }

    public List<CouponTemplateInfo> searchTemplate(CouponTemplateInfo request) {
        CouponTemplate example =CouponTemplate.builder()
                .shopId(request.getShopId())
                .category(CouponType.convert(request.getType()))
                .available(request.getAvailable())
                .name(request.getName())
                .build();
        // 可以用下面的方式做分页查询
//        Pageable page = PageRequest.of(0, 100);
//        templateDao.findAll(Example.of(example), page);
        List<CouponTemplate> result = templateDao.findAll(Example.of(example));
        return result.stream()
                .map(CouponTemplateConverter::convertToTemplateInfo)
                .collect(Collectors.toList());
    }

    @Override
    public CouponTemplateInfo loadTemplateInfo(Long id) {
        Optional<CouponTemplate> template = templateDao.findById(id);
        return template.isPresent() ? CouponTemplateConverter.convertToTemplateInfo(template.get()) : null;
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        int rows = templateDao.makeCouponUnavailable(id);
        if (rows == 0) {
            throw new IllegalArgumentException("Template Not Found: " + id);
        }
    }

    /**
     * 批量读取模板
     * @param ids
     * @return
     */
    @Override
    public Map<Long, CouponTemplateInfo> getTemplateInfoMap(Collection<Long> ids) {
        List<CouponTemplate> templates = templateDao.findAllById(ids);

        return templates.stream()
                .map(CouponTemplateConverter::convertToTemplateInfo)
                .collect(Collectors.toMap(CouponTemplateInfo::getId, Function.identity()));
    }

    // 将券无效化
    @Override
    @Transactional
    public void deleteTemplateTCC(Long id) {
        CouponTemplate filter = CouponTemplate.builder()
                .available(true)
                .locked(false)
                .id(id)
                .build();

        CouponTemplate template = templateDao.findAll(Example.of(filter))
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Template Not Found"));

        template.setLocked(true);
        templateDao.save(template);
    }

    @Override
    @Transactional
    public void deleteTemplateCommit(BusinessActionContext context) {
        Long id = Long.parseLong(context.getActionContext("id").toString());

        CouponTemplate template = templateDao.findById(id).get();

        template.setLocked(false);
        template.setAvailable(false);
        templateDao.save(template);

        log.info("TCC committed");
    }

    @Override
    @Transactional
    public void deleteTemplateCancel(BusinessActionContext context) {
        Long id = Long.parseLong(context.getActionContext("id").toString());
        Optional<CouponTemplate> templateOption = templateDao.findById(id);

        if (templateOption.isPresent()) {
            CouponTemplate template = templateOption.get();
            template.setLocked(false);
            templateDao.save(template);
        }
        log.info("TCC cancel");
    }
}
