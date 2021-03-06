package io.onedev.server.web.editable;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

@SuppressWarnings("serial")
public abstract class ValueEditor<T> extends FormComponentPanel<T> implements ErrorContext {

	public ValueEditor(String id, IModel<T> model) {
		super(id, model);
		setConvertedInput(model.getObject());
	}

	public @Nullable ErrorContext getErrorContext(ValuePath partPath) {
		ErrorContext context = this;
		for (PathSegment segment: partPath.getElements()) {
			ErrorContext segmentContext = context.getErrorContext(segment);
			if (segmentContext != null)
				context = segmentContext;
			else 
				break;
		}
		return context;
	}
	
	public void clearErrors(boolean recursive) {
		if (recursive) {
			visitFormComponentsPostOrder(this, new IVisitor<FormComponent<?>, Void>() {
				
				@Override
				public void component(FormComponent<?> formComponent, IVisit<Void> visit) {
					formComponent.getFeedbackMessages().clear();
				}
				
			});
		} else {
			getFeedbackMessages().clear();
		}
	}

	/**
	 * Get error context for specified path segment. 
	 * 
	 * @return
	 * 			error context of specified path segment, or <tt>null</tt> if error context 
	 * 			for specified path segment can not be found
	 */
	public abstract @Nullable ErrorContext getErrorContext(PathSegment pathSegment);

	protected abstract T convertInputToValue() throws ConversionException;
	
	@Override
	public void convertInput() {
		try {
			setConvertedInput(convertInputToValue());
		} catch (ConversionException e) {
			error(newValidationError(e));
		}
	}
	
	@Override
	public void addError(String errorMessage) {
		error(errorMessage);
	}

	@Override
	public boolean hasErrors(boolean recursive) {
		if (recursive)
			return !isValid();
		else
			return hasErrorMessage();
	}

}
