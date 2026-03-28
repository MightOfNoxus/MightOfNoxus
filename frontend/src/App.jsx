import { useEffect, useState } from 'react'
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  Layout,
  List,
  message,
  Row,
  Select,
  Space,
  Statistic,
  Switch,
  Tabs,
  Tag,
  Typography,
  InputNumber
} from 'antd'
import {
  createAgent,
  createReactAgent,
  createSkill,
  createTool,
  fetchAgents,
  fetchSkills,
  fetchSummary,
  fetchTools
} from './services/api'

const { Header, Content } = Layout
const { TextArea } = Input

function App() {
  const [agentForm] = Form.useForm()
  const [toolForm] = Form.useForm()
  const [skillForm] = Form.useForm()
  const [reactAgentForm] = Form.useForm()

  const [summary, setSummary] = useState({ agents: 0, tools: 0, skills: 0, readyToBuild: false })
  const [agents, setAgents] = useState([])
  const [tools, setTools] = useState([])
  const [skills, setSkills] = useState([])
  const [reactAgentResult, setReactAgentResult] = useState(null)
  const [creatingReactAgent, setCreatingReactAgent] = useState(false)

  const loadData = async () => {
    const [summaryRes, agentsRes, toolsRes, skillsRes] = await Promise.all([
      fetchSummary(),
      fetchAgents(),
      fetchTools(),
      fetchSkills()
    ])
    setSummary(summaryRes)
    setAgents(agentsRes)
    setTools(toolsRes)
    setSkills(skillsRes)
  }

  useEffect(() => {
    loadData().catch(() => message.error('加载数据失败，请确认后端服务已启动'))
  }, [])

  const handleCreate = async (creator, values, successText, form) => {
    try {
      await creator(values)
      message.success(successText)
      form.resetFields()
      await loadData()
    } catch {
      message.error('保存失败，请检查输入内容')
    }
  }

  const handleCreateReactAgent = async (values) => {
    setCreatingReactAgent(true)
    try {
      const res = await createReactAgent(values)
      setReactAgentResult(res)
      message.success('ReactAgent 创建成功')
    } catch (error) {
      message.error(error?.response?.data?.message || 'ReactAgent 创建失败，请检查模型配置')
    } finally {
      setCreatingReactAgent(false)
    }
  }

  return (
    <Layout className="page-layout">
      <Header className="app-header">
        <Typography.Title level={3} style={{ color: '#fff', margin: 0 }}>
          智能体配置平台（Spring AI Alibaba + React）
        </Typography.Title>
      </Header>
      <Content className="app-content">
        <Row gutter={16} className="stat-row">
          <Col span={8}><Card><Statistic title="Agent 配置数" value={summary.agents} /></Card></Col>
          <Col span={8}><Card><Statistic title="MCP 工具数" value={summary.tools} /></Card></Col>
          <Col span={8}><Card><Statistic title="Skill 配置数" value={summary.skills} /></Card></Col>
        </Row>

        <Card style={{ marginBottom: 16 }}>
          <Space>
            <Tag color={summary.readyToBuild ? 'success' : 'processing'}>
              {summary.readyToBuild ? '可快速搭建智能体' : '请至少创建一个Agent和技能/工具'}
            </Tag>
          </Space>
        </Card>

        <Card style={{ marginBottom: 16 }} title="一键创建 ReactAgent（基于配置）">
          <Form form={reactAgentForm} layout="vertical" onFinish={handleCreateReactAgent}>
            <Form.Item name="agentId" label="选择Agent" rules={[{ required: true, message: '请选择Agent' }]}>
              <Select
                style={{ width: 260 }}
                placeholder="选择已配置 Agent"
                options={agents.map(agent => ({ label: `${agent.name} (${agent.modelProvider}/${agent.modelName})`, value: agent.id }))}
              />
            </Form.Item>
            <Form.Item name="userMessage" label="试运行消息">
              <Input placeholder="可选：输入消息后会直接调用一次" style={{ width: 480 }} />
            </Form.Item>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name={['runtimeParams', 'requestId']} label="requestId">
                  <Input placeholder="REQ-20260328-001" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name={['runtimeParams', 'tenant']} label="tenant">
                  <Input placeholder="default-tenant" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name={['hookOptions', 'maxModelCalls']} label="最大模型调用次数">
                  <InputNumber min={1} max={20} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name={['hookOptions', 'enableToolRetry']} label="启用 ToolRetry" valuePropName="checked">
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name={['hookOptions', 'toolRetryTimes']} label="ToolRetry 次数">
                  <InputNumber min={1} max={5} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name={['hookOptions', 'enableContextEditing']} label="启用上下文压缩" valuePropName="checked">
                  <Switch />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={creatingReactAgent}>创建 ReactAgent</Button>
            </Form.Item>
          </Form>

          {reactAgentResult && (
            <Card style={{ marginTop: 16 }} type="inner" title={`创建结果：${reactAgentResult.agentName}`}>
              <p><b>状态：</b>{reactAgentResult.status}</p>
              <p><b>TraceId：</b>{reactAgentResult.traceId}</p>
              <p><b>已应用执行Hook：</b>{(reactAgentResult.appliedHooks || []).join(', ') || '无'}</p>
              <p><b>最终提示词：</b></p>
              <pre className="prompt-preview">{reactAgentResult.finalPrompt}</pre>
              {reactAgentResult.answer && <p><b>试运行响应：</b>{reactAgentResult.answer}</p>}
            </Card>
          )}
        </Card>

        <Tabs
          items={[
            {
              key: 'agent',
              label: 'Agent 配置',
              children: (
                <Row gutter={16}>
                  <Col span={12}>
                    <Card title="新建 Agent">
                      <Form layout="vertical" onFinish={(v) => handleCreate(createAgent, v, 'Agent 创建成功', agentForm)} form={agentForm}>
                        <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
                        <Form.Item name="description" label="描述"><Input /></Form.Item>
                        <Form.Item name="modelProvider" label="模型供应商" rules={[{ required: true }]}><Input placeholder="DashScope / OpenAI / Others" /></Form.Item>
                        <Form.Item name="modelName" label="模型名称" rules={[{ required: true }]}><Input placeholder="qwen-max" /></Form.Item>
                        <Form.Item name="promptTemplate" label="系统提示词模板" rules={[{ required: true }]}><TextArea rows={4} /></Form.Item>
                        <Button type="primary" htmlType="submit">保存 Agent</Button>
                      </Form>
                    </Card>
                  </Col>
                  <Col span={12}>
                    <Card title="Agent 列表">
                      <List dataSource={agents} renderItem={(item) => <List.Item>{item.name} · {item.modelProvider}/{item.modelName}</List.Item>} />
                    </Card>
                  </Col>
                </Row>
              )
            },
            {
              key: 'tool',
              label: 'MCP 工具配置',
              children: (
                <Row gutter={16}>
                  <Col span={12}>
                    <Card title="新建 MCP 工具">
                      <Form layout="vertical" onFinish={(v) => handleCreate(createTool, v, 'MCP 工具创建成功', toolForm)} form={toolForm}>
                        <Form.Item name="name" label="工具名" rules={[{ required: true }]}><Input /></Form.Item>
                        <Form.Item name="endpoint" label="服务地址" rules={[{ required: true }]}><Input /></Form.Item>
                        <Form.Item name="protocol" label="协议" rules={[{ required: true }]}><Input placeholder="SSE / HTTP / WS" /></Form.Item>
                        <Form.Item name="description" label="描述"><Input /></Form.Item>
                        <Button type="primary" htmlType="submit">保存工具</Button>
                      </Form>
                    </Card>
                  </Col>
                  <Col span={12}>
                    <Card title="MCP 工具列表">
                      <List dataSource={tools} renderItem={(item) => <List.Item>{item.name} · {item.protocol}</List.Item>} />
                    </Card>
                  </Col>
                </Row>
              )
            },
            {
              key: 'skill',
              label: 'Skill 配置',
              children: (
                <Row gutter={16}>
                  <Col span={12}>
                    <Card title="新建 Skill">
                      <Form layout="vertical" onFinish={(v) => handleCreate(createSkill, v, 'Skill 创建成功', skillForm)} form={skillForm}>
                        <Form.Item name="name" label="Skill 名称" rules={[{ required: true }]}><Input /></Form.Item>
                        <Form.Item name="category" label="分类" rules={[{ required: true }]}><Input /></Form.Item>
                        <Form.Item name="version" label="版本" rules={[{ required: true }]}><Input placeholder="v1.0.0" /></Form.Item>
                        <Form.Item name="instructions" label="技能说明" rules={[{ required: true }]}><TextArea rows={4} /></Form.Item>
                        <Button type="primary" htmlType="submit">保存 Skill</Button>
                      </Form>
                    </Card>
                  </Col>
                  <Col span={12}>
                    <Card title="Skill 列表">
                      <List dataSource={skills} renderItem={(item) => <List.Item>{item.name} · {item.category}</List.Item>} />
                    </Card>
                  </Col>
                </Row>
              )
            }
          ]}
        />
      </Content>
    </Layout>
  )
}

export default App
