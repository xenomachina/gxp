/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gxp.compiler.reparent;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.ContentTypeExpectedAlert;
import com.google.gxp.compiler.alerts.common.InvalidAttributeValueError;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.NoDefaultValueForConditionalArgumentError;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.base.AbbrExpression;
import com.google.gxp.compiler.base.BooleanConstant;
import com.google.gxp.compiler.base.BooleanType;
import com.google.gxp.compiler.base.BundleType;
import com.google.gxp.compiler.base.ClassImport;
import com.google.gxp.compiler.base.CollapseExpression;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.Constructor;
import com.google.gxp.compiler.base.ContentType;
import com.google.gxp.compiler.base.ConvertibleToContent;
import com.google.gxp.compiler.base.CppFileImport;
import com.google.gxp.compiler.base.CppLibraryImport;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.ImplementsDeclaration;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.JavaAnnotation;
import com.google.gxp.compiler.base.LoopExpression;
import com.google.gxp.compiler.base.MultiLanguageAttrValue;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NativeImplementsDeclaration;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.NoMessage;
import com.google.gxp.compiler.base.NullRoot;
import com.google.gxp.compiler.base.ObjectConstant;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.PackageImport;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.PlaceholderEnd;
import com.google.gxp.compiler.base.PlaceholderStart;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.SpaceOperator;
import com.google.gxp.compiler.base.SpaceOperatorSet;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.TemplateType;
import com.google.gxp.compiler.base.ThrowsDeclaration;
import com.google.gxp.compiler.base.Type;
import com.google.gxp.compiler.base.UnboundCall;
import com.google.gxp.compiler.base.UnboundImplementsDeclaration;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.UnextractedMessage;
import com.google.gxp.compiler.ifexpand.IfExpandedTree;
import com.google.gxp.compiler.parser.CallNamespace;
import com.google.gxp.compiler.parser.CppNamespace;
import com.google.gxp.compiler.parser.DefaultingParsedElementVisitor;
import com.google.gxp.compiler.parser.ExprNamespace;
import com.google.gxp.compiler.parser.GxpNamespace;
import com.google.gxp.compiler.parser.JavaNamespace;
import com.google.gxp.compiler.parser.MsgNamespace;
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.parser.NamespaceVisitor;
import com.google.gxp.compiler.parser.NoMsgNamespace;
import com.google.gxp.compiler.parser.NullElement;
import com.google.gxp.compiler.parser.NullNamespace;
import com.google.gxp.compiler.parser.OutputLanguageNamespace;
import com.google.gxp.compiler.parser.OutputNamespace;
import com.google.gxp.compiler.parser.ParsedAttribute;
import com.google.gxp.compiler.parser.ParsedElement;
import com.google.gxp.compiler.parser.ParsedElementVisitor;
import com.google.gxp.compiler.parser.TextElement;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.DocType;
import com.google.gxp.compiler.schema.ElementValidator;
import com.google.gxp.compiler.schema.Schema;
import com.google.gxp.compiler.schema.SchemaFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Moves <code>gxp:attr</code>, <code>gxp:param</code>, <code>gxp:import</code>
 * and <code>gxp:throws</code> into their parent elements.  Also converts
 * attributes into some type of "attribute node" that shares a superclass with
 * whatever <code>gxp:attr</code>s turn into, at least for elements that can
 * contain <code>gxp:attr</code>s.
 */
public class Reparenter implements Function<IfExpandedTree, ReparentedTree> {
  private static final String DEFAULT_CONTENT_TYPE = "text/html";

  private final SchemaFactory schemaFactory;
  private final String className;
  private Schema rootSchema = null;

  public Reparenter(SchemaFactory schemaFactory, String className) {
    this.schemaFactory = Objects.nonNull(schemaFactory);
    this.className = Objects.nonNull(className);
  }

  public ReparentedTree apply(IfExpandedTree parseTree) {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(parseTree.getAlerts());

    // make sure root element is a template
    List<ParsedElement> children = parseTree.getChildren();
    if (!children.isEmpty()) {
      ParsedElement first = children.get(0);
      if (!first.canBeRoot()) {
        alertSetBuilder.add(new InvalidRootError(first));
      } else {
        // determine the schema of this gxp
        String contentType = DEFAULT_CONTENT_TYPE;
        for (ParsedAttribute attr : first.getAttributes()) {
          if (attr.getName().equals("content-type")
              && attr.getNamespace() == NullNamespace.INSTANCE) {
            contentType = attr.getValue();
          }
        }
        rootSchema = schemaFactory.fromContentTypeName(contentType);
      }
    }

    Iterable<ParsedAttribute> noAttrs = Collections.emptyList();
    Parts parts = groupParts(alertSetBuilder, parseTree, noAttrs, children);

    // Throw out all but the first Root. The parse phase should have
    // already complained about there being too many roots.
    List<Root> roots = parts.getRoots();
    Root root = roots.isEmpty()
        ? new NullRoot(parseTree, TemplateName.parseFullyQualifiedDottedName(className))
        : roots.get(0);
    return new ReparentedTree(parseTree.getSourcePosition(),
                              alertSetBuilder.buildAndClear(),
                              root);
  }

  /**
   * Converts a {@code ParsedAttribute} into an {@code Attribute}.
   */
  private static Attribute convertAttribute(final ParsedAttribute parsedAttr) {
    Namespace namespace = parsedAttr.getNamespace();
    return namespace.acceptVisitor(
        new NamespaceVisitor<Attribute>() {
          public Attribute visitCallNamespace(CallNamespace ns) {
            throw new Error("TODO(laurence): implement");
          }

          public Attribute visitCppNamespace(CppNamespace ns) {
            return new Attribute(parsedAttr, ns, parsedAttr.getName(),
                                 new StringConstant(parsedAttr, null,
                                                    parsedAttr.getValue()),
                                 null, null);
          }

          public Attribute visitExprNamespace(ExprNamespace ns) {
            return new Attribute(parsedAttr, NullNamespace.INSTANCE,
                                 parsedAttr.getName(),
                                 new NativeExpression(parsedAttr,
                                                      parsedAttr.getValue(),
                                                      null, null),
                                 null, null);
          }

          public Attribute visitGxpNamespace(GxpNamespace ns) {
            return new Attribute(parsedAttr, ns, parsedAttr.getName(),
                                 new StringConstant(parsedAttr, null,
                                                    parsedAttr.getValue()),
                                 null, null);
          }

          public Attribute visitJavaNamespace(JavaNamespace ns) {
            return new Attribute(parsedAttr, ns, parsedAttr.getName(),
                                 new StringConstant(parsedAttr, null,
                                                    parsedAttr.getValue()),
                                 null, null);
          }

          public Attribute visitMsgNamespace(MsgNamespace ns) {
            Expression str = new StringConstant(parsedAttr, null, parsedAttr.getValue());
            Expression msg = new UnextractedMessage(parsedAttr, null, null, null, false, str);
            return new Attribute(parsedAttr, NullNamespace.INSTANCE, parsedAttr.getName(),
                                 new ConvertibleToContent(msg),
                                 null, null);
          }

          public Attribute visitNoMsgNamespace(NoMsgNamespace ns) {
            Expression str = new StringConstant(parsedAttr, null, parsedAttr.getValue());
            Expression nomsg = new NoMessage(parsedAttr, str);
            return new Attribute(parsedAttr, NullNamespace.INSTANCE, parsedAttr.getName(),
                                 new ConvertibleToContent(nomsg),
                                 null, null);
          }

          public Attribute visitNullNamespace(NullNamespace ns) {
            return new Attribute(parsedAttr, ns, parsedAttr.getName(),
                                 new StringConstant(parsedAttr, null,
                                                    parsedAttr.getValue()),
                                 null, null);
          }

          public Attribute visitOutputNamespace(OutputNamespace ns) {
            throw new Error("TODO(laurence): implement");
          }
        });
  }

  /**
   * Converts a list of {@code ParsedAttribute}s and a list of {@code
   * ParsedNode}s into a {@code Parts} structure. It does this by converting
   * each element that it comes across into a higher-level type (Attribute,
   * Import, etc.) and then placing each converted element into the appropriate
   * part bucket.
   */
  private Parts groupParts(final AlertSink alertSink,
                           final Node forNode,
                           Iterable<ParsedAttribute> parsedAttrs,
                           Iterable<ParsedElement> children) {
    final EditableParts result = new EditableParts(alertSink, forNode);
    ParsedElementVisitor<Void> childVisitor = new DefaultingParsedElementVisitor<Void>() {
      public Void defaultVisitElement(ParsedElement element) {
        Parts nodeParts = groupParts(alertSink,
                                     element,
                                     element.getAttributes(),
                                     element.getChildren());
        element.acceptVisitor(new ElementVisitor(result, nodeParts, alertSink));
        nodeParts.reportUnused();
        return null;
      }
    };
    for (ParsedAttribute parsedAttr : parsedAttrs) {
      result.accumulate(convertAttribute(parsedAttr));
    }
    for (ParsedElement child : children) {
      child.acceptVisitor(childVisitor);
    }
    return result;
  }


  private class ElementVisitor implements ParsedElementVisitor<Void> {
    private final EditableParts output;
    private final Parts nodeParts;
    private final AlertSink alertSink;

    /**
     * @param output where we should place the thing that we're building.
     * @param nodeParts the parts of the node we're visiting, which go into the
     * what we're building.
     */
    ElementVisitor(EditableParts output, Parts nodeParts, AlertSink alertSink) {
      this.output = Objects.nonNull(output);
      this.nodeParts = Objects.nonNull(nodeParts);
      this.alertSink = Objects.nonNull(alertSink);
    }

    private SpaceOperator parseSpaceOperator(AttributeMap attrMap,
                                             Namespace ns,
                                             String name) {
      String value = attrMap.getOptional(ns, name, null);
      if (value != null) {
        SpaceOperator result =
            SpaceOperator.valueOf(value.trim().toUpperCase());
        if (result == null) {
          alertSink.add(
              new InvalidAttributeValueError(attrMap.getAttribute(ns, name)));
        }
        return result;
      } else {
        return null;
      }
    }

    private SpaceOperatorSet getSpaceOperators(AttributeMap attrMap) {
      SpaceOperator interiorSpaceOperator = parseSpaceOperator(
          attrMap, GxpNamespace.INSTANCE, "ispace");
      SpaceOperator exteriorSpaceOperator = parseSpaceOperator(
          attrMap, GxpNamespace.INSTANCE, "espace");
      return new SpaceOperatorSet(interiorSpaceOperator, exteriorSpaceOperator);
    }

    /**
     * @param defaultElement the default {@code JavaAnnotation.Element} to use for
     * {@code JavaAnnotation}s that don't already have a
     * {@code JavaAnnotation.Element}.
     * @param rest other {@JavaAnnotation.Element}s that are allowed in this
     * context.
     */
    private List<JavaAnnotation> getJavaAnnotations(Node parent,
                                                    JavaAnnotation.Element defaultElement,
                                                    JavaAnnotation.Element... rest) {
      List<JavaAnnotation.Element> allowedElements = Lists.newArrayList(rest);
      allowedElements.add(defaultElement);

      List<JavaAnnotation> result = Lists.newArrayList();
      for (JavaAnnotation annotation : nodeParts.getJavaAnnotations()) {
        if (annotation.getElement() == null) {
          result.add(annotation.withElement(defaultElement));
        } else if (allowedElements.contains(annotation.getElement())) {
          result.add(annotation);
        } else {
          alertSink.add(new MisplacedJavaAnnotationError(annotation));
        }
      }

      // Check for java:annotate attribute
      AttributeMap attrMap = nodeParts.getAttributes();
      Attribute annotateAttr = attrMap.getAttribute(JavaNamespace.INSTANCE, "annotate");
      if (annotateAttr != null) {
        String annotateStr = annotateAttr.getValue().getStaticString(alertSink, "");
        result.add(new JavaAnnotation(annotateAttr, defaultElement, annotateStr));
      }

      return result;
    }

    private Expression getCollapsableContent(AttributeMap attrMap) {
      Expression content = nodeParts.getContent();
      return CollapseExpression.create(content, getSpaceOperators(attrMap));
    }

    public Void visitAttrElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String name = attrMap.get("name", null);
      if (name != null) {
        // TODO(laurence): make it possible to have attr elements for other
        // namespaces?
        Expression content =
            new ConvertibleToContent(getCollapsableContent(attrMap));
        Expression condition = attrMap.getOptionalExprValue("cond", null);
        output.accumulate(new Attribute(node, name, content, condition));
      }
      return null;
    }

    // TODO(harryh): there might be a problem here if gxp:eval doesn't
    //               have a plain text expr (either expr:expr or
    //               <gxp:attr name='expr'>
    public Void visitEvalElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String expr = attrMap.get("expr", null);
      String example = attrMap.getOptional("example", null);
      String phName = attrMap.getOptional(GxpNamespace.INSTANCE, "ph", null);
      if (expr != null) {
        output.accumulate(new NativeExpression(node, expr, example, phName));
      }
      return null;
    }

    public Void visitCondElement(GxpNamespace.GxpElement node) {
      List<Conditional.Clause> clauses = nodeParts.getClauses();
      Expression elseExpression = null;

      // add MissingAttributeError if cond isn't supplied for a clause
      // that isn't the last (or if it's the only) clause
      //
      // TODO(harryh): stricly speaking a single Clause shouldn't have to have a
      // clause, but we need to add an alert in the case of a <gxp:if> without
      // a cond and it's tricky to do that elsewhere
      Iterator<Conditional.Clause> iter = clauses.iterator();
      while (iter.hasNext()) {
        Conditional.Clause clause = iter.next();
        if (clause.getPredicate().alwaysEquals(true)
            && (clauses.size() == 1 || iter.hasNext() == true)) {
          alertSink.add(new MissingAttributeError(clause, "cond"));
        }
      }

      if (!clauses.isEmpty()) {
        Conditional.Clause lastClause = clauses.get(clauses.size() - 1);
        if (lastClause.getPredicate().alwaysEquals(true)) {
          elseExpression = lastClause.getExpression();
          clauses = clauses.subList(0, clauses.size() - 1);
        } else {
          elseExpression = new StringConstant(node, null, "");
        }
      }

      if (clauses.isEmpty()) {
        if (elseExpression == null) {
          alertSink.add(new NoClausesInCondError(node));
        }
      } else {
        output.accumulate(new Conditional(node, null, clauses,
                                          elseExpression));
      }

      return null;
    }

    public Void visitClauseElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();


      Expression predicate =
          attrMap.getOptionalExprValue("cond",
                                       new BooleanConstant(node, true));
      if (predicate != null) {
        output.accumulate(
            new Conditional.Clause(node, predicate,
                                   getCollapsableContent(attrMap)));
      }
      return null;
    }

    public Void visitIfElement(GxpNamespace.GxpElement node) {
      throw new UnexpectedNodeException(node);
    }

    public Void visitElifElement(GxpNamespace.GxpElement node) {
      throw new UnexpectedNodeException(node);
    }

    public Void visitElseElement(GxpNamespace.GxpElement node) {
      throw new UnexpectedNodeException(node);
    }

    public Void visitImportElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();

      String cls = attrMap.getOptional("class", null);
      String pkg = attrMap.getOptional("package", null);

      if (cls == null && pkg == null) {
        alertSink.add(new MissingAttributesError(node, "class", "package"));
        return null;
      }

      if (cls != null && pkg != null) {
        alertSink.add(new ConflictingAttributesError(
                          node,
                          attrMap.getAttribute(NullNamespace.INSTANCE, "class"),
                          attrMap.getAttribute(NullNamespace.INSTANCE, "package")));
        return null;
      }

      if (pkg != null) {
        // TODO(harryh): strictly speaking this will incorrectly reject
        //               package='foo', but this will prolly never matter
        //               in real life.
        TemplateName.FullyQualified packageName =
            TemplateName.parseFullyQualifiedDottedName(
                alertSink, node.getSourcePosition(), pkg);
        if (packageName != null) {
          output.accumulate(new PackageImport(node, pkg));
        }
      } else if (cls != null) {
        TemplateName.FullyQualified className =
            TemplateName.parseFullyQualifiedDottedName(
                alertSink, node.getSourcePosition(), cls);
        if (className != null) {
          output.accumulate(new ClassImport(node, className));
        }
      }
      return null;
    }

    private TemplateName.FullyQualified createRootName(Node node) {
      AttributeMap attrMap = nodeParts.getAttributes();

      Expression nameAttr = attrMap.getValue("name", null);
      if (nameAttr != null) {
        String s = nameAttr.getStaticString(alertSink, null);
        if (!className.equals(s)) {
          alertSink.add(new MismatchedTemplateNameError(
                            nameAttr.getSourcePosition(), s, className));
        }
      }

      return TemplateName.parseFullyQualifiedDottedName(
          alertSink, node.getSourcePosition(), className);
    }

    public Void visitInterfaceElement(GxpNamespace.GxpElement node) {
      TemplateName.FullyQualified name = createRootName(node);
      ContentType contentType = createContentType(node, DEFAULT_CONTENT_TYPE);

      // add a "this" parameter to the list of Parameters
      List<Parameter> parameters = Lists.newArrayList(nodeParts.getParameters());
      FormalParameter formal = new FormalParameter(node.getSourcePosition(),
                                                   Implementable.INSTANCE_PARAM_NAME,
                                                   Implementable.INSTANCE_PARAM_NAME,
                                                   new TemplateType(node.getSourcePosition(),
                                                                    name.toString(), name));
      parameters.add(new Parameter(formal));

      List<JavaAnnotation> javaAnnotations =
          getJavaAnnotations(node, JavaAnnotation.Element.INTERFACE);
      List<Import> imports = nodeParts.getImports();
      List<ThrowsDeclaration> throwsDeclarations = nodeParts.getThrowsDeclarations();
      List<FormalTypeParameter> formalTypeParameters = nodeParts.getFormalTypeParameters();

      if (contentType != null) {
        output.accumulate(new Interface(node,
                                        name,
                                        contentType.getSchema(),
                                        javaAnnotations,
                                        imports,
                                        throwsDeclarations,
                                        parameters,
                                        formalTypeParameters));
      }

      return null;
    }

    public Void visitAbbrElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      Type type = createType(node, attrMap, false, null);
      String name = getVariableName(attrMap, "name");
      Expression expr = attrMap.getExprValue("expr", null);
      Expression content = getCollapsableContent(attrMap);
      if (type != null && name != null && expr != null) {
        output.accumulate(new AbbrExpression(node, type, name, expr, content));
      }
      return null;
    }

    public Void visitLoopElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      Type type = createType(node, attrMap, false, null);
      String var = getVariableName(attrMap, "var");

      Attribute delimiterAttr = attrMap.getAttribute("delimiter");
      Expression delimiter = (delimiterAttr == null)
          ? new StringConstant(node, null, " ")
          : delimiterAttr.getValue();

      if (delimiterAttr != null && delimiterAttr.getCondition() != null) {
        alertSink.add(new NoDefaultValueForConditionalArgumentError(node, "delimiter"));
      }

      Expression iterator = attrMap.getOptionalExprValue("iterator", null);
      Expression iterable = attrMap.getOptionalExprValue("iterable", null);

      if (iterable == null && iterator == null) {
        alertSink.add(new MissingAttributesError(node, "iterator", "iterable"));
        return null;
      }

      if (iterable != null && iterator != null) {
        alertSink.add(new ConflictingAttributesError(
                          node,
                          attrMap.getAttribute(NullNamespace.INSTANCE, "iterator"),
                          attrMap.getAttribute(NullNamespace.INSTANCE, "iterable")));
        return null;
      }

      if ((type != null) && (var != null)) {
        if (iterable != null) {
          output.accumulate(
              LoopExpression.createWithIterable(
                  node, type, var, iterable, getCollapsableContent(attrMap),
                  delimiter));
        } else if (iterator != null) {
          output.accumulate(
              LoopExpression.createWithIterator(
                  node, type, var, iterator, getCollapsableContent(attrMap),
                  delimiter));
        }
      }
      return null;
    }

    /**
     * @return a {@code Type} based on {@code gxpType}.  Optionally grab
     * additional attributes from the {@code AttributeMap}
     */
    private Type createGxpType(Node node, AttributeMap attrMap, Attribute gxpType) {
      String kind = gxpType.getValue().getStaticString(alertSink, null);

      GxpType type = GxpType.parse(kind);
      if (type == null) {
        return null;
      }
      switch(type) {
        case BOOL:
          return new BooleanType(node);
        case BUNDLE:
          String from = attrMap.get("from-element", null);
          if (from == null || rootSchema == null) {
            return null;
          }

          Map<String, AttributeValidator> subAttrMap = Maps.newHashMap(
              rootSchema.getElementValidator(from).getAttributeValidatorMap());

          // remove items from the exclude list
          String exclude = attrMap.getOptional("exclude", null);
          if (exclude != null) {
            for (String eItem : exclude.split(",")) {
              // TODO(harryh): make sure items in exclude list
              //               actually remove elements
              subAttrMap.remove(eItem.trim());
            }
          }

          // make sure that bundle doesn't accept any required attributes
          Iterator<Map.Entry<String, AttributeValidator>> iter
              = subAttrMap.entrySet().iterator();
          while (iter.hasNext()) {
            Map.Entry<String, AttributeValidator> entry = iter.next();
            if (entry.getValue().isFlagSet(AttributeValidator.Flag.REQUIRED)) {
              alertSink.add(new RequiredAttrInBundleError(node,
                                                          entry.getKey()));
              iter.remove();
            }
          }

          return new BundleType(node, rootSchema, subAttrMap);
        default:
          alertSink.add(new InvalidAttributeValueError(gxpType));
          return null;
      }
    }

    /**
     * @return a {@code Type} based on the attribures contained in the
     *         passed in {@code AttributeMap}.
     */
    private Type createType(Node node, AttributeMap attrMap, boolean forParam, Type defaultType) {
      Attribute gxpType = attrMap.getAttribute(GxpNamespace.INSTANCE, "type");
      Attribute contentType = attrMap.getAttribute(NullNamespace.INSTANCE, "content-type");
      MultiLanguageAttrValue nativeType = attrMap.getMultiLanguageAttrValue("type");

      // check for conflicting attributes
      if (gxpType != null && contentType != null) {
        alertSink.add(new ConflictingAttributesError(node, gxpType, contentType));
      }
      if ((gxpType != null || contentType != null) && !nativeType.isEmpty()) {
        Attribute conflict = (gxpType != null) ? gxpType : contentType;
        for (OutputLanguageNamespace ns : AttributeMap.getOutputLanguageNamespaces()) {
          Attribute nsAttr = attrMap.getAttribute(ns, "type");
          if (nsAttr != null) {
            alertSink.add(new ConflictingAttributesError(node, conflict, nsAttr));
          }
        }
        Attribute defaultTypeAttr = attrMap.getAttribute("type");
        if (defaultTypeAttr != null) {
          alertSink.add(new ConflictingAttributesError(node, conflict, defaultTypeAttr));
        }
      }

      // build an appropriate type
      Type type = (gxpType != null) ? createGxpType(node, attrMap, gxpType)
          : (contentType != null) ? createContentType(node, null)
          : (defaultType == null || !nativeType.isEmpty()) ? new NativeType(node, nativeType)
          : defaultType;

      if (type != null && type.onlyAllowedInParam() && !forParam) {
        alertSink.add(new InvalidTypeError(type));
        type = null;
      }

      return type;
    }

    /**
     * Parses an attribute to produce a ContentType.
     *
     * @param forNode Node ContentType is for
     * @param defaultValue default value in case the attribute is
     * unset/invalid, or null if there is no default
     */
    private ContentType createContentType(Node forNode, String defaultValue) {
      AttributeMap attrMap = nodeParts.getAttributes();
      Attribute attr = attrMap.getAttribute("content-type");

      Node node;
      String contentType;
      if (attr == null) {
        node = forNode;
        contentType = defaultValue;
      } else {
        node = attr;
        contentType = attr.getValue().getStaticString(alertSink, defaultValue);
      }

      Schema schema = (contentType == null)
          ? null
          : schemaFactory.fromContentTypeName(contentType);

      if (schema == null && contentType != null) {
        alertSink.add(new UnknownContentTypeError(node, contentType));
      }

      return (schema == null)
          ? null
          : new ContentType(node.getSourcePosition(),
                            node.getDisplayName(),
                            schema);
    }

    public Void visitParamElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String name = getVariableName(attrMap, "name");
      List<JavaAnnotation> javaAnnotations
          = getJavaAnnotations(node, JavaAnnotation.Element.PARAM);
      Expression defaultValue = null;
      boolean hasDefaultFlag = false;
      Pattern regex = null;
      Expression constructor = null;
      boolean hasConstructorFlag = false;

      String content = attrMap.getOptional("content", null);
      Type defaultType = null;
      boolean consumesContent = content != null;
      if (consumesContent) {
        if (!"*".equals(content)) {
          alertSink.add(new InvalidAttributeValueError(attrMap.getAttribute("content")));
        }
        if (rootSchema != null) {
          defaultType = new ContentType(node.getSourcePosition(),
                                        node.getDisplayName(),
                                        rootSchema);
        }
      }

      Type type = createType(node, attrMap, true, defaultType);
      if (type == null) {
        // Bail out without trying to construct Parameter.
        return null;
      }
      if (consumesContent && !type.isContent()) {
        alertSink.add(new ContentTypeExpectedAlert(node.getSourcePosition(),
                                                   node.getDisplayName(),
                                                   "when content='*' is set."));
        type = defaultType;
      }

      if (type.takesDefaultParam()) {
        defaultValue = attrMap.getOptionalExprValue("default", null);
        hasDefaultFlag = attrMap.getBooleanValue("has-default");
      }

      if (type.takesRegexParam()) {
        String regexStr = attrMap.getOptional("regex", null);
        if (regexStr != null) {
          try {
            regex = Pattern.compile(regexStr);
          } catch (PatternSyntaxException e) {
            alertSink.add(new BadRegexError(node, e.getPattern()));
          }
        }
      } else {
        regex = type.getPattern(name);
      }

      if (type.takesConstructorParam()) {
        constructor = attrMap.getOptionalExprValue("constructor", null);
        hasConstructorFlag = attrMap.getBooleanValue("has-constructor");
      }

      SpaceOperatorSet spaceOperators = getSpaceOperators(attrMap);

      // Note that we *don't* want to run the comment through space collapsing
      // as the space operators specified on a gxp:param actually apply to the
      // passed values.
      // TODO(laurence): should gxp:param even support nested content?
      Expression comment = nodeParts.getContent();
      if (!comment.hasStaticString()) {
        alertSink.add(new RequiresStaticContentError(node));
        comment = new StringConstant(node, null, "");
      }
      if (name != null) {
        FormalParameter formal = new FormalParameter(node, name, consumesContent, type,
                                                     defaultValue, hasDefaultFlag, regex,
                                                     constructor, hasConstructorFlag,
                                                     spaceOperators);
        output.accumulate(new Parameter(formal, javaAnnotations, defaultValue, hasDefaultFlag,
                                        constructor, hasConstructorFlag, comment));
      }
      return null;
    }

    public Void visitTemplateElement(GxpNamespace.GxpElement node) {
      TemplateName.FullyQualified name = createRootName(node);

      AttributeMap attrMap = nodeParts.getAttributes();

      ContentType contentType = createContentType(node, DEFAULT_CONTENT_TYPE);

      List<Constructor> constructors = nodeParts.getConstructors();
      Constructor constructor = constructors.isEmpty()
          ? Constructor.empty(node) : constructors.get(0);

      if (constructors.size() > 1) {
        alertSink.add(new MoreThanOneConstructorError(constructors.get(1)));
      }

      List<JavaAnnotation> javaAnnotations = getJavaAnnotations(node,
                                                                JavaAnnotation.Element.CLASS,
                                                                JavaAnnotation.Element.INSTANCE,
                                                                JavaAnnotation.Element.INTERFACE);

      List<Import> imports = nodeParts.getImports();
      List<ImplementsDeclaration> implementsDeclarations = nodeParts.getImplementsDeclarations();
      List<ThrowsDeclaration> throwsDeclarations = nodeParts.getThrowsDeclarations();
      List<Parameter> parameters = nodeParts.getParameters();
      List<FormalTypeParameter> formalTypeParameters = nodeParts.getFormalTypeParameters();
      Expression content = getCollapsableContent(attrMap);

      if (contentType != null) {
        output.accumulate(new Template(node,
                                       name,
                                       contentType.getSchema(),
                                       javaAnnotations,
                                       constructor,
                                       imports,
                                       implementsDeclarations,
                                       throwsDeclarations,
                                       parameters,
                                       formalTypeParameters,
                                       content));
      }

      return null;
    }

    public Void visitConstructorElement(GxpNamespace.GxpElement node) {
      output.accumulate(new Constructor(node,
                                        getJavaAnnotations(node,
                                                           JavaAnnotation.Element.CONSTRUCTOR),
                                        nodeParts.getParameters()));
      return null;
    }

    public Void visitImplementsElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String interfaceType = attrMap.getOptional(JavaNamespace.INSTANCE, "interface", null);
      if (interfaceType != null) {
        NativeType type = new NativeType(node, interfaceType);
        output.accumulate(new NativeImplementsDeclaration(node, type));
      } else {
        interfaceType = attrMap.get("interface", null);
        if (interfaceType != null) {
          TemplateName templateName = TemplateName.create(null, interfaceType);
          output.accumulate(new UnboundImplementsDeclaration(node, templateName));
        }
      }
      return null;
    }

    public Void visitThrowsElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String exceptionType = attrMap.get("exception", null);
      if (exceptionType != null) {
        TemplateName exception =
            TemplateName.parseDottedName(
                alertSink, node.getSourcePosition(), exceptionType);
        if (exception != null) {
          // TODO(harryh): exceptionType should be a NativeType, not just
          //               a String
          output.accumulate(new ThrowsDeclaration(node, exceptionType));
        }
      }
      return null;
    }

    public Void visitTypeParamElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String name = getVariableName(attrMap, "name");
      String extendsType = attrMap.getOptional("extends", null);
      if (name != null) {
        NativeType type = (extendsType == null)
            ? null : new NativeType(node, extendsType);
        output.accumulate(new FormalTypeParameter(node, name, type));
      }
      return null;
    }

    public Void visitMsgElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String meaning = attrMap.getOptional("meaning", null);
      String comment = attrMap.getOptional("comment", null);
      boolean hidden = attrMap.getBooleanValue("hidden");
      // TODO(laurence): coerce content to HTML
      Expression content = getCollapsableContent(attrMap);

      ContentType contentType = createContentType(node, null);
      Schema schema = (contentType == null) ? null : contentType.getSchema();

      output.accumulate(new UnextractedMessage(node, schema, meaning, comment, hidden, content));
      return null;
    }

    public Void visitNoMsgElement(GxpNamespace.GxpElement node) {
      // TODO(laurence) suppress i18n warnings (when they exist)
      AttributeMap attrMap = nodeParts.getAttributes();
      output.accumulate(new NoMessage(node, getCollapsableContent(attrMap)));
      return null;
    }

    public Void visitPHElement(GxpNamespace.GxpElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String name = attrMap.get("name", null);
      String example = attrMap.getOptional("example", null);
      if (example != null) {
        if (example.trim().length() == 0) {
          alertSink.add(
              new InvalidAttributeValueError(attrMap.getAttribute("example")));
          example = "<var>" + name + "</var>";
        }
      }
      if (name != null) {
        output.accumulate(new PlaceholderStart(node, null, name, example));
      }
      return null;
    }

    public Void visitEPHElement(GxpNamespace.GxpElement node) {
      output.accumulate(new PlaceholderEnd(node, null));
      return null;
    }

    /**
     * Examine the {@code gxp:bundles} attribute and return the list of
     * bundles it specifies.
     */
    private List<String> getBundles(AttributeMap attrMap) {
      List<String> bundles = Lists.newArrayList();
      String bundlesStr = attrMap.getOptional(GxpNamespace.INSTANCE, "bundles",
                                              null);
      if (bundlesStr != null) {
        for (String s : bundlesStr.split(",")) {
          bundles.add(s.trim());
        }
      }
      return bundles;
    }

    // CallNamespace elements
    public Void visitCallElement(CallNamespace.CallElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      ImmutableMap.Builder<String, Attribute> attrBuilder =
          ImmutableMap.builder();
      Expression content =
          new ConvertibleToContent(getCollapsableContent(attrMap));
      List<String> bundles = getBundles(attrMap);
      for (Attribute attr : attrMap.getUnusedAttributes()) {
        if (attr.getNamespace() instanceof NullNamespace) {
          // non expr: attributes are treated differently for "new" and "old" style
          // calls.  For old calls they are NativeExpressions.  For new ones,
          // they are ObjectConstants
          Expression value = attr.getValue();
          if (value instanceof StringConstant) {
            attr = attr.withValue(node.allAttrsAreExpr()
                                  ? attr.getExprValue()
                                  : new ObjectConstant((StringConstant) value));
          }
          attrBuilder.put(attr.getName(), attr);
        } else {
          alertSink.add(new UnknownAttributeError(node, attr));
        }
      }

      TemplateName callee = TemplateName.parseDottedName(
          alertSink,
          node.getSourcePosition(),
          node.getTagName());
      if (callee.isValid()) {
        // TODO(laurence): if callee == null then substitute good one
        output.accumulate(
            new UnboundCall(node.getSourcePosition(),
                            node.getDisplayName(),
                            callee,
                            attrBuilder.build(),
                            bundles,
                            content));
      }
      return null;
    }

    // OutputNamespace elements
    public Void visitParsedOutputElement(
        OutputNamespace.ParsedOutputElement node) {
      ElementValidator validator = node.getValidator();
      AttributeMap attrMap = nodeParts.getAttributes();

      String docTypeName =
          attrMap.getOptional(GxpNamespace.INSTANCE, "doctype", null);
      DocType docType;
      if (docTypeName == null) {
        docType = null;
      } else {
        docType = validator.getDocType(docTypeName);
        if (docType == null) {
          alertSink.add(new InvalidDoctypeError(node, docTypeName));
        }
      }

      // if an output element has a gxp:ph attribute we surrount the element
      // tags (but NOT the element content) with placeholders
      String phName = attrMap.getOptional(GxpNamespace.INSTANCE, "ph", null);

      // TODO(laurence): always collect content here, and check against
      // NOENDTAG in Validator phase.
      Expression content;
      if (validator.isFlagSet(ElementValidator.Flag.NOENDTAG)) {
        // Ignore supplied children. EditableParts will generate alerts about
        // them if necessary.
        content = new StringConstant(node, null, "");
      } else {
        content = getCollapsableContent(attrMap);
      }

      String innerContentTypeString = validator.getInnerContentType();
      Schema innerSchema = (innerContentTypeString == null)
          ? null
          : schemaFactory.fromContentTypeName(innerContentTypeString);

      List<String> bundles = getBundles(attrMap);

      List<Attribute> attrs = attrMap.getUnusedAttributes();
      output.accumulate(
          new OutputElement(node.getSourcePosition(),
                            node.getDisplayName(),
                            node.getSchema(),
                            innerSchema,
                            validator.getTagName(),
                            validator,
                            docType,
                            checkAttributes(node, validator, attrs),
                            bundles,
                            phName,
                            content));
      return null;
    }

    // CppNamespace elements
    public Void visitCppIncludeElement(CppNamespace.CppElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();

      String libraryName = attrMap.getOptional("library", null);
      String fileName = attrMap.getOptional("file", null);

      if (libraryName == null && fileName == null) {
        alertSink.add(new MissingAttributesError(node, "library", "file"));
        return null;
      }

      if (libraryName != null && fileName != null) {
        alertSink.add(new ConflictingAttributesError(
                          node,
                          attrMap.getAttribute(NullNamespace.INSTANCE, "library"),
                          attrMap.getAttribute(NullNamespace.INSTANCE, "file")));
        return null;
      }

      if (libraryName != null) {
        output.accumulate(new CppLibraryImport(node, libraryName));
      } else if (fileName != null) {
        output.accumulate(new CppFileImport(node, fileName));
      }

      return null;
    }

    // JavaNamespace elements
    public Void visitJavaAnnotateElement(JavaNamespace.JavaElement node) {
      AttributeMap attrMap = nodeParts.getAttributes();
      String with = attrMap.get("with", null);
      JavaAnnotation.Element element = null;
      String elementStr = attrMap.getOptional("element", null);
      if (elementStr != null) {
        try {
          element = JavaAnnotation.Element.valueOf(elementStr.toUpperCase());
        } catch (IllegalArgumentException e) {
          alertSink.add(new InvalidAttributeValueError(attrMap.getValue("element", null)));
          with = null;
        }
      }

      if (with != null) {
        output.accumulate(new JavaAnnotation(node.getSourcePosition(),
                                             node.getDisplayName(),
                                             element,
                                             with));
      }
      return null;
    }

    public Void visitTextElement(TextElement node) {
      output.accumulate(new StringConstant(node, null, node.getText()));
      return null;
    }

    // ignore NullElements (created by IfExpander in error cases)
    public Void visitNullElement(NullElement node) {
      return null;
    }

    /**
     * Checks attributes based on element validator. This involves
     * removing unknown attributes, and reporting these and any other
     * attribute problems as {@code Alert}s.
     *
     * The main purpose of this function is actually reporting these issues,
     * but they are also "corrected" to some  degree in order to allow
     * processing to continue without tripping over these issues again
     * later on in the pipeline.
     *
     * In addition, Attributes that have an inner content-type are modified
     * to indicate this fact.
     *
     * @param elementValidator ElementValidator to be used in checking
     * all attributes
     * @param attrs the attributes to filter
     * @return a copy of {@code attrs} with "corrections"
     */
    private List<Attribute> checkAttributes(Node forNode, ElementValidator elementValidator,
                                            List<Attribute> attrs) {
      if (attrs.isEmpty()) {
        return Collections.emptyList();
      } else {
        List<Attribute> result = Lists.newArrayList();
        for (Attribute attr : attrs) {
          AttributeValidator attrValidator =
              elementValidator.getAttributeValidator(attr.getName());
          if (attrValidator == null) {
            alertSink.add(new UnknownAttributeError(forNode, attr));
          } else {
            Expression attrValue = attr.getValue();
            if (attrValue instanceof StringConstant) {
              String value = ((StringConstant) attrValue).evaluate();
              if (!attrValidator.isValidValue(value)) {
                alertSink.add(new InvalidAttributeValueError(attr));
              }
            }

            // if the attribute validator indicates an inner content type then
            // set this on the attribute
            String innerContentTypeString = attrValidator.getContentType();
            if (innerContentTypeString != null) {
              Schema innerSchema = schemaFactory.fromContentTypeName(innerContentTypeString);
              attr = attr.withInnerSchema(innerSchema);
            }

            result.add(attr);
          }
        }
        return result;
      }
    }

    private String getVariableName(AttributeMap attrMap, String attrName) {
      String result = attrMap.get(attrName, null);
      if (result != null) {
        if (!VARIABLE_NAME_PATTERN.matcher(result).matches()) {
          alertSink.add(new IllegalVariableNameError(attrMap.getAttribute(attrName), result));
          return null;
        }

        if (ILLEGAL_NAME_PATTERN.matcher(result).matches()) {
          alertSink.add(new IllegalVariableNameError(attrMap.getAttribute(attrName), result));
          return null;
        }

        if (result.length() > 64) {
          alertSink.add(new IllegalVariableNameError(attrMap.getAttribute(attrName), result));
          return null;
        }
      }
      return result;
    }
  }

  // Starts with an ASCII letter, followed by (ASCII) letters, numbers and
  // underscores. Underscores may only appear between two non-underscores.
  private static final Pattern VARIABLE_NAME_PATTERN =
      Pattern.compile("[a-zA-Z](_?[a-zA-Z0-9])*");

  // "this" is reserved for use with gxp:interface
  // "gxp_" prefix is reserved for internal use (gxp_context, gxp_locale)
  private static final Pattern ILLEGAL_NAME_PATTERN =
      Pattern.compile("this|gxp_(.)*");

  private enum GxpType {
    BOOL, BUNDLE;

    private static final Map<String, GxpType> MAP =
      ImmutableMap.<String, GxpType>builder()
        .put("boolean", GxpType.BOOL)
        .put("bundle", GxpType.BUNDLE)
        .build();

    public static GxpType parse(String s) {
      return MAP.get(s);
    }
  }
}
