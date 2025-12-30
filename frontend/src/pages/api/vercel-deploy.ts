import type { NextApiRequest, NextApiResponse } from 'next'

type DeployStatusResponse = {
  status: string
  timestamp: string
  environment: string
  version: string
  buildTime: string
  deploymentUrl?: string
}

export default function handler(
  req: NextApiRequest,
  res: NextApiResponse<DeployStatusResponse>
) {
  const deployData: DeployStatusResponse = {
    status: 'deployed',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || 'development',
    version: process.env.NEXT_PUBLIC_APP_VERSION || '1.0.0',
    buildTime: process.env.BUILD_TIME || new Date().toISOString(),
    deploymentUrl: process.env.VERCEL_URL ? `https://${process.env.VERCEL_URL}` : undefined
  }

  res.status(200).json(deployData)
}
