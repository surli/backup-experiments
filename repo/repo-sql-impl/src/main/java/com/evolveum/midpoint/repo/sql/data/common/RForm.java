/*
 * Copyright (c) 2010-2017 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.repo.sql.data.common;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.data.RepositoryContext;
import com.evolveum.midpoint.repo.sql.data.common.embedded.RPolyString;
import com.evolveum.midpoint.repo.sql.util.DtoTranslationException;
import com.evolveum.midpoint.repo.sql.util.IdGeneratorResult;
import com.evolveum.midpoint.repo.sql.util.MidPointJoinedPersister;
import com.evolveum.midpoint.repo.sql.util.RUtil;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FormType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Persister;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Collection;
import java.util.Objects;

/**
 * @author mederly
 */
@Entity
@ForeignKey(name = "fk_form")
@Table(uniqueConstraints = @UniqueConstraint(name = "uc_form_name", columnNames = {"name_norm"}))
@Persister(impl = MidPointJoinedPersister.class)
public class RForm extends RObject<FormType> {

    private RPolyString name;

    @Embedded
    public RPolyString getName() {
        return name;
    }

    public void setName(RPolyString name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RForm))
            return false;
        if (!super.equals(o))
            return false;
        RForm rForm = (RForm) o;
        return Objects.equals(name, rForm.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

	public static void copyFromJAXB(FormType jaxb, RForm repo, RepositoryContext repositoryContext,
			IdGeneratorResult generatorResult) throws DtoTranslationException {
		RObject.copyFromJAXB(jaxb, repo, repositoryContext, generatorResult);
	}


	@Override
    public FormType toJAXB(PrismContext prismContext, Collection<SelectorOptions<GetOperationOptions>> options)
            throws DtoTranslationException {
        FormType object = new FormType();
        RUtil.revive(object, prismContext);
        RForm.copyToJAXB(this, object, prismContext, options);
        return object;
    }
}
