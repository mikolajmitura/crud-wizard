package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import pl.jalokim.crudwizard.core.sample.SamplePersonDto

class DepartmentDto {

    String id
    DepartmentName name
    String departmentName
    DepartmentDto superDepartment
    SamplePersonDto headOfDepartment

    enum DepartmentName {
        SIMPLE, EXTENDED
    }
}
