package com.iit.msc.ase.tmf.customermanagement.external.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.iit.msc.ase.tmf.commonconfig.application.exception.type.CustomerMgtException;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.repository.CustomerRepository;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.AccountRefService;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.AgreementRefService;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.CharacteristicService;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.ContactMediumService;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.CreditProfileService;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.CustomerService;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.EngagedPartyService;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.PaymentRefService;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.RelatedPartyRefService;
import com.iit.msc.ase.tmf.customermanagement.domain.dto.feature.CreateCustomerReqDto;
import com.iit.msc.ase.tmf.customermanagement.domain.dto.feature.CreateCustomerRespDto;
import com.iit.msc.ase.tmf.customermanagement.domain.dto.feature.QueryAllCustomerRespDto;
import com.iit.msc.ase.tmf.customermanagement.domain.dto.feature.QueryCustomerByIdRespDto;
import com.iit.msc.ase.tmf.customermanagement.domain.dto.feature.UpdateCustomerReqDto;
import com.iit.msc.ase.tmf.customermanagement.domain.dto.feature.UpdateCustomerRespDto;
import com.iit.msc.ase.tmf.customermanagement.domain.dto.headers.ResponseHeaderDto;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.AccountRef;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.AgreementRef;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.Characteristic;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.ContactMedium;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.CreditProfile;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.Customer;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.EngagedParty;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.PaymentRef;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.RelatedParty;
import com.iit.msc.ase.tmf.customermanagement.domain.model.customer.TimePeriod;
import com.iit.msc.ase.tmf.customermanagement.external.util.Constants;
import com.iit.msc.ase.tmf.datamodel.domain.dto.AccountRefDto;
import com.iit.msc.ase.tmf.datamodel.domain.dto.AgreementRefDto;
import com.iit.msc.ase.tmf.datamodel.domain.dto.CharacteristicDto;
import com.iit.msc.ase.tmf.datamodel.domain.dto.ContactMediumDto;
import com.iit.msc.ase.tmf.datamodel.domain.dto.CreditProfileDto;
import com.iit.msc.ase.tmf.datamodel.domain.dto.CustomerDto;
import com.iit.msc.ase.tmf.datamodel.domain.dto.EngagedPartyDto;
import com.iit.msc.ase.tmf.datamodel.domain.dto.PaymentRefDto;
import com.iit.msc.ase.tmf.datamodel.domain.dto.RelatedPartyDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Value( "${validation.regex.offset}" )
    private String validationRegexOffset;

    @Value( "${validation.regex.limit}" )
    private String validationRegexLimit;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRefService accountRefService;

    @Autowired
    private AgreementRefService agreementRefService;

    @Autowired
    private CharacteristicService characteristicService;

    @Autowired
    private RelatedPartyRefService relatedPartyRefService;

    @Autowired
    private PaymentRefService paymentRefService;

    @Autowired
    private CreditProfileService creditProfileService;

    @Autowired
    private ContactMediumService contactMediumService;

    @Autowired
    private EngagedPartyService engagedPartyService;

    @Override
    public CreateCustomerRespDto create(CreateCustomerReqDto createCustomerReqDto) {
        log("create method of Customer started");
        Customer customer = getModelMapper().map(createCustomerReqDto.getCustomer(), Customer.class);//mapping basic parameters

        customer.setAccount(getAccountRefsList(createCustomerReqDto.getCustomer()));
        customer.setAgreement(getAgreementRefsList(createCustomerReqDto.getCustomer()));
        customer.setCharacteristic(getCharacteristicsList(createCustomerReqDto.getCustomer()));
        customer.setPaymentMethod(getPaymentMethodList(createCustomerReqDto.getCustomer()));
        customer.setRelatedParty(getRelatedPartiesList(createCustomerReqDto.getCustomer()));
        customer.setCreditProfile(getCreditProfileList(createCustomerReqDto.getCustomer()));
        customer.setContactMedium(getContactMediumList(createCustomerReqDto.getCustomer()));
        customer.setEngagedParty(getEngagedPartyList(createCustomerReqDto.getCustomer()));

        CreateCustomerRespDto createCustomerRespDto = new CreateCustomerRespDto();
        ResponseHeaderDto responseHeaderDto = new ResponseHeaderDto();
        customerRepository.save(customer);
        responseHeaderDto.setResponseCode(String.valueOf(HttpStatus.OK.value()));
        responseHeaderDto.setResponseDesc(Constants.OPERATION_SUCCESSFUL);
        responseHeaderDto.setResponseDescDisplay(Constants.CXM1000);

        responseHeaderDto.setRequestId(createCustomerReqDto.getRequestHeader().getRequestId());
        responseHeaderDto.setTimestamp(LocalDateTime.now().toString());
        createCustomerRespDto.setResponseHeader(responseHeaderDto);
        log("create method of Customer ended");
        return createCustomerRespDto;
    }

    @Override
    public QueryAllCustomerRespDto queryAll(Map < String, String > filters, String fields, Integer offset, Integer limit) throws CustomerMgtException {
        log("queryAll method of Customer started");
        validateOffset(offset);
        validateLimit(limit);
        QueryAllCustomerRespDto queryAllCustomerRespDto = new QueryAllCustomerRespDto();
        ResponseHeaderDto responseHeaderDto = new ResponseHeaderDto();
        Pageable requestedPage = PageRequest.of(offset - 1, limit);
        List < Customer > customerList;
        customerList = findByFilters(filters, fields, requestedPage, offset, limit);
        if ( !customerList.isEmpty() ) {
            queryAllCustomerRespDto.setResponseData(customerList);
            responseHeaderDto.setResponseDescDisplay(Constants.CXM1000);
            responseHeaderDto.setResponseCode(String.valueOf(HttpStatus.OK.value()));
            responseHeaderDto.setResponseDesc(Constants.OPERATION_SUCCESSFUL);
        } else {
            responseHeaderDto.setResponseDescDisplay(Constants.CXM2000);
            responseHeaderDto.setResponseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            responseHeaderDto.setResponseDesc("No records found");
        }
        responseHeaderDto.setTimestamp(LocalDateTime.now().toString());
        responseHeaderDto.setRequestId("123");
        queryAllCustomerRespDto.setResponseHeader(responseHeaderDto);
        log("queryAll method of Customer ended");
        return queryAllCustomerRespDto;
    }

    @Override
    public QueryCustomerByIdRespDto queryById(String id) {
        log("queryById method of Customer started|id:{}", id);
        QueryCustomerByIdRespDto queryCustomerByIdRespDto = new QueryCustomerByIdRespDto();
        ResponseHeaderDto responseHeaderDto = new ResponseHeaderDto();
        Optional < Customer > customer = customerRepository.findById(id);
        if ( customer.isPresent() ) {
            queryCustomerByIdRespDto.setResponseData(customer.get());
            responseHeaderDto.setResponseDescDisplay(Constants.CXM1000);
            responseHeaderDto.setResponseCode(String.valueOf(HttpStatus.OK.value()));
            responseHeaderDto.setResponseDesc(Constants.OPERATION_SUCCESSFUL);
        } else {
            responseHeaderDto.setResponseDescDisplay(Constants.CXM2000);
            responseHeaderDto.setResponseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            responseHeaderDto.setResponseDesc("Customer not found");
        }
        responseHeaderDto.setTimestamp(LocalDateTime.now().toString());
        responseHeaderDto.setRequestId("123");
        queryCustomerByIdRespDto.setResponseHeader(responseHeaderDto);
        log("queryById method of Customer ended");
        return queryCustomerByIdRespDto;
    }

    @Override
    public void deleteById(String id) {
        log("deleteById method of Customer started|id:{}", id);
        customerRepository.deleteById(id);
        log("deleteById method of Customer ended");
    }

    @Override
    public UpdateCustomerRespDto update(String id, UpdateCustomerReqDto updateCustomerReqDto) {
        log("update method of Customer started");
        Customer customer = findById(id);
        customer.setStatus(updateCustomerReqDto.getCustomer().getStatus());
        customer.setType(updateCustomerReqDto.getCustomer().getType());
        customer.setStatusReason(updateCustomerReqDto.getCustomer().getStatusReason());
        customer.setBaseType(updateCustomerReqDto.getCustomer().getBaseType());
        customer.setName(updateCustomerReqDto.getCustomer().getName());
        customer.setSchemaLocation(updateCustomerReqDto.getCustomer().getSchemaLocation());
        customer.setValidFor(getModelMapper().map(updateCustomerReqDto.getCustomer().getValidFor(), TimePeriod.class));

        customer.setAccount(getAccountRefsList(updateCustomerReqDto.getCustomer()));
        customer.setAgreement(getAgreementRefsList(updateCustomerReqDto.getCustomer()));
        customer.setCharacteristic(getCharacteristicsList(updateCustomerReqDto.getCustomer()));
        customer.setPaymentMethod(getPaymentMethodList(updateCustomerReqDto.getCustomer()));
        customer.setRelatedParty(getRelatedPartiesList(updateCustomerReqDto.getCustomer()));
        customer.setCreditProfile(getCreditProfileList(updateCustomerReqDto.getCustomer()));
        customer.setContactMedium(getContactMediumList(updateCustomerReqDto.getCustomer()));
        customer.setEngagedParty(getEngagedPartyList(updateCustomerReqDto.getCustomer()));

        UpdateCustomerRespDto updateCustomerRespDto = new UpdateCustomerRespDto();
        ResponseHeaderDto responseHeaderDto = new ResponseHeaderDto();
        customerRepository.save(customer);
        responseHeaderDto.setResponseCode(String.valueOf(HttpStatus.OK.value()));
        responseHeaderDto.setResponseDesc(Constants.OPERATION_SUCCESSFUL);
        responseHeaderDto.setResponseDescDisplay(Constants.CXM1000);

        responseHeaderDto.setRequestId(updateCustomerReqDto.getRequestHeader().getRequestId());
        responseHeaderDto.setTimestamp(LocalDateTime.now().toString());
        updateCustomerRespDto.setResponseHeader(responseHeaderDto);
        log("create method of Customer ended");
        return updateCustomerRespDto;
    }

    @Override
    public Customer findById(String id) {
        log("findByReferredType method of EngagedParty started");
        Optional < Customer > customerOptional = customerRepository.findById(id);
        return customerOptional.orElse(null);
    }

    /**
     * @param limit
     * @throws CustomerMgtException
     */
    private void validateLimit(Integer limit) throws CustomerMgtException {
        if ( !Pattern.matches(validationRegexLimit, limit.toString()) || limit <= 0 ) {
            logger.error("Invalid limit:{}", limit);
            throw new CustomerMgtException(String.format("Invalid limit:%s", limit), Constants.CXM2002);
        }
    }

    /**
     * @param offset
     * @throws CustomerMgtException
     */
    private void validateOffset(Integer offset) throws CustomerMgtException {
        if ( !Pattern.matches(validationRegexOffset, offset.toString()) || offset <= 0 ) {
            logger.error("Invalid offset:{}", offset);
            throw new CustomerMgtException(String.format("Invalid offset:%s", offset), Constants.CXM2001);
        }
    }

    /**
     * @param filters
     * @param fields
     * @param requestedPage
     * @param pageNumber
     * @param pageSize
     * @return
     */
    private List < Customer > findByFilters(Map < String, String > filters, String fields, Pageable requestedPage, Integer pageNumber, Integer pageSize) {
        log("findByFilters method started");
        MatchOperation matchStage = null;
        ProjectionOperation projectStage = null;
        Aggregation aggregation = null;
        if ( !filters.isEmpty() ) {
            filters.remove(Constants.OFFSET_KEY);
            filters.remove(Constants.LIMIT_KEY);
            if ( fields != null ) {
                filters.remove(Constants.FIELDS_KEY);
            }
            List < Criteria > criteriaList = new ArrayList <>();
            for ( Map.Entry < String, String > entry : filters.entrySet() ) {
                Criteria criteria = Criteria.where(entry.getKey()).in(entry.getValue());
                criteriaList.add(criteria);
            }
            matchStage = new MatchOperation(!criteriaList.isEmpty() ? new Criteria().andOperator(criteriaList.toArray(new Criteria[ criteriaList.size() ])) : new Criteria());
        }
        if ( fields != null ) {
            List < String > requiredFieldList = Stream.of(fields.split(",", -1)).collect(Collectors.toList());
            projectStage = Aggregation.project(requiredFieldList.toArray(new String[ 0 ]));//projectStage = Aggregation.project("href", "status");//at lease 1 param should be there
        }

        if ( !filters.isEmpty() ) {
            if ( pageNumber.equals(1) ) {
                aggregation = Aggregation.newAggregation(matchStage, limit(pageSize));//no need to pass skip param if you need the first page
            } else {
                aggregation = Aggregation.newAggregation(matchStage, skip((pageNumber - 1) * pageSize), limit(pageSize));
            }
        }

        if ( fields != null ) {
            if ( pageNumber.equals(1) ) {
                aggregation = Aggregation.newAggregation(projectStage, limit(pageSize));
            } else {
                aggregation = Aggregation.newAggregation(projectStage, skip((pageNumber - 1) * pageSize), limit(pageSize));
            }
        }

        if ( !filters.isEmpty() && fields != null ) {
            if ( pageNumber.equals(1) ) {
                aggregation = Aggregation.newAggregation(matchStage, projectStage, limit(pageSize));
            } else {
                aggregation = Aggregation.newAggregation(matchStage, projectStage, skip((pageNumber - 1) * pageSize), limit(pageSize));//, projectStage, skip(pageNumber * pageSize), limit(pageSize)
            }
        }

        if ( filters.isEmpty() && fields == null ) {
            log("findByFilters method ended");
            return customerRepository.findAll(requestedPage).getContent();
        }

        AggregationResults < Customer > result = mongoTemplate.aggregate(aggregation, "customer", Customer.class);
        log("findByFilters method ended");
        return result.getMappedResults();
    }

    private List < EngagedParty > getEngagedPartyList(CustomerDto customerDto) {
        List < EngagedPartyDto > engagedPartyDtoList = customerDto.getEngagedParty();
        List < EngagedParty > engagedPartyList = new ArrayList <>();
        for ( EngagedPartyDto engagedPartyDto : engagedPartyDtoList ) {
            if ( engagedPartyDto.getId() == null ) {
                engagedPartyList.add(engagedPartyService.create(getModelMapper().map(engagedPartyDto, EngagedParty.class)));
            } else {
                //find by @id
                EngagedParty existingEngagedParty = engagedPartyService.findById(engagedPartyDto.getId());
                if ( existingEngagedParty != null ) {
                    engagedPartyList.add(existingEngagedParty);
                } else {
                    //create a new record and add to list
                    engagedPartyList.add(engagedPartyService.create(getModelMapper().map(engagedPartyDto, EngagedParty.class)));
                }
            }
        }
        return engagedPartyList;
    }

    private List < ContactMedium > getContactMediumList(CustomerDto customerDto) {
        List < ContactMediumDto > contactMediumDtoList = customerDto.getContactMedium();
        List < ContactMedium > contactMediumList = new ArrayList <>();
        for ( ContactMediumDto contactMediumDto : contactMediumDtoList ) {
            //find by @Type
            List < ContactMedium > existingContactMediumList = contactMediumService.findByReferredType(contactMediumDto.getReferredType());
            if ( existingContactMediumList != null ) {
                if ( !existingContactMediumList.isEmpty() ) {
                    contactMediumList.add(existingContactMediumList.get(0));
                } else {
                    contactMediumList.add(contactMediumService.create(getModelMapper().map(contactMediumDto, ContactMedium.class)));
                }
            } else {
                //create a new record and add to list
                contactMediumList.add(contactMediumService.create(getModelMapper().map(contactMediumDto, ContactMedium.class)));
            }
        }
        return contactMediumList;
    }

    private List < CreditProfile > getCreditProfileList(CustomerDto customerDto) {
        List < CreditProfileDto > creditProfileDtoList = customerDto.getCreditProfile();
        List < CreditProfile > creditProfileList = new ArrayList <>();
        for ( CreditProfileDto creditProfileDto : creditProfileDtoList ) {
            //find by @Type
            List < CreditProfile > existingCreditProfileList = creditProfileService.findByType(creditProfileDto.getType());
            if ( existingCreditProfileList != null ) {
                if ( !existingCreditProfileList.isEmpty() ) {
                    creditProfileList.add(existingCreditProfileList.get(0));
                } else {
                    creditProfileList.add(creditProfileService.create(getModelMapper().map(creditProfileDto, CreditProfile.class)));
                }
            } else {
                //create a new record and add to list
                creditProfileList.add(creditProfileService.create(getModelMapper().map(creditProfileDto, CreditProfile.class)));
            }
        }
        return creditProfileList;
    }

    private List < PaymentRef > getPaymentMethodList(CustomerDto customerDto) {
        List < PaymentRefDto > paymentRefDtoList = customerDto.getPaymentMethod();
        List < PaymentRef > paymentRefList = new ArrayList <>();
        for ( PaymentRefDto paymentRefDto : paymentRefDtoList ) {
            if ( paymentRefDto.getId() == null ) {
                paymentRefList.add(paymentRefService.create(getModelMapper().map(paymentRefDto, PaymentRef.class)));
            } else {
                //find by @referredType
                PaymentRef existingPaymentRef = paymentRefService.findById(paymentRefDto.getId());
                if ( existingPaymentRef != null ) {
                    paymentRefList.add(existingPaymentRef);
                } else {
                    //create a new record and add to list
                    paymentRefList.add(paymentRefService.create(getModelMapper().map(paymentRefDto, PaymentRef.class)));
                }
            }
        }
        return paymentRefList;
    }

    private List < AccountRef > getAccountRefsList(CustomerDto customerDto) {
        List < AccountRefDto > accountRefDtoList = customerDto.getAccount();
        List < AccountRef > accountRefList = new ArrayList <>();
        for ( AccountRefDto accountRefDto : accountRefDtoList ) {
            if ( accountRefDto.getId() == null ) {
                //create a new record and add to list
                accountRefList.add(accountRefService.create(getModelMapper().map(accountRefDto, AccountRef.class)));
            } else {
                //find by id
                AccountRef existingAccountRef = accountRefService.findById(accountRefDto.getId());
                if ( existingAccountRef != null ) {
                    accountRefList.add(existingAccountRef);
                } else {
                    //create a new record and add to list
                    accountRefList.add(accountRefService.create(getModelMapper().map(accountRefDto, AccountRef.class)));
                }
            }
        }
        return accountRefList;
    }

    private List < AgreementRef > getAgreementRefsList(CustomerDto customerDto) {
        List < AgreementRefDto > agreementRefDtoList = customerDto.getAgreement();
        List < AgreementRef > agreementRefList = new ArrayList <>();
        for ( AgreementRefDto agreementRefDto : agreementRefDtoList ) {
            if ( agreementRefDto.getId() == null ) {
                //create a new record and add to list
                agreementRefList.add(agreementRefService.create(getModelMapper().map(agreementRefDto, AgreementRef.class)));
            } else {
                //find by @id
                AgreementRef existingAgreementRef = agreementRefService.findById(agreementRefDto.getId());
                if ( existingAgreementRef != null ) {
                    agreementRefList.add(existingAgreementRef);
                } else {
                    //create a new record and add to list
                    agreementRefList.add(agreementRefService.create(getModelMapper().map(agreementRefDto, AgreementRef.class)));
                }
            }
        }
        return agreementRefList;
    }

    private List < Characteristic > getCharacteristicsList(CustomerDto customerDto) {
        List < CharacteristicDto > characteristicDtoList = customerDto.getCharacteristic();
        List < Characteristic > characteristicList = new ArrayList <>();
        for ( CharacteristicDto characteristicDto : characteristicDtoList ) {
            //find by name
            List < Characteristic > existingCharacteristicList = characteristicService.findByName(characteristicDto.getName());
            if ( existingCharacteristicList != null ) {
                if ( !existingCharacteristicList.isEmpty() ) {
                    characteristicList.add(existingCharacteristicList.get(0));
                } else {
                    characteristicList.add(characteristicService.create(getModelMapper().map(characteristicDto, Characteristic.class)));
                }
            } else {
                //create a new record and add to list
                characteristicList.add(characteristicService.create(getModelMapper().map(characteristicDto, Characteristic.class)));
            }
        }
        return characteristicList;
    }

    private List < RelatedParty > getRelatedPartiesList(CustomerDto customerDto) {
        List < RelatedPartyDto > relatedPartyDtoList = customerDto.getRelatedParty();
        List < RelatedParty > relatedPartyList = new ArrayList <>();
        for ( RelatedPartyDto relatedPartyDto : relatedPartyDtoList ) {
            if ( relatedPartyDto.getId() == null ) {
                relatedPartyList.add(relatedPartyRefService.create(getModelMapper().map(relatedPartyDto, RelatedParty.class)));
            } else {
                //find by id
                RelatedParty existingRelatedParty = relatedPartyRefService.findById(relatedPartyDto.getId());
                if ( existingRelatedParty != null ) {
                    relatedPartyList.add(existingRelatedParty);
                } else {
                    //create a new record and add to list
                    relatedPartyList.add(relatedPartyRefService.create(getModelMapper().map(relatedPartyDto, RelatedParty.class)));
                }
            }
        }
        return relatedPartyList;
    }

    public ModelMapper getModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

}
